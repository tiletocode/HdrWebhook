package com.hdr.whatap.webhook;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class Receiver extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		log.warn("There is no service for GET method : [ " + request.getRemoteAddr() + " / " + request.getRequestURI()
				+ " ]");
		throw new ServletException("There is no service for GET method");

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Config config = Config.getConfig();

		String allowValues = config.getString("webhook.event.types", "app,infra,db,kube");
		String eventType = request.getParameter(config.getString("webhook.server.uri.paramName", "param"));

		if (allowValues.indexOf(eventType) < 0) {
			String message = eventType + " is not allowed event type";
			log.error(message);
			throw new ServletException(message);
		}

		String path = config.getString("webhook." + eventType + ".file.path", null);
		if (path == null) {
			String message = "eventType is not allowed";
			log.error(message);
			throw new NullPointerException(message);
		}

		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		String requestBody = jb.toString();
		ObjectMapper om = new ObjectMapper();
		WebhookDto dto = om.readValue(requestBody, WebhookDto.class);

		// DB이벤트의 null_replace값을 치환
		if (eventType.equals(config.getString("webhook.event.db.type", "db"))) {
			dto.setMetricName(dto.getMetricName() != null ? dto.getMetricName()
					: config.getString("webhook.message.db.metricName.nullreplace", "DB"));
			dto.setMetricThreshold(dto.getMetricThreshold() != null ? dto.getMetricThreshold()
					: config.getString("webhook.message.db.metricThreshold.nullreplace", "DB"));
			dto.setMetricValue(dto.getMetricValue() != null ? dto.getMetricValue()
					: config.getString("webhook.message.db.metricValue.nullreplace", "DB"));
			dto.setOname(dto.getOname() != null ? dto.getOname()
					: config.getString("webhook.message.db.oname.nullreplace", "DB"));
		} else {
			dto.setMetricName(dto.getMetricName() != null ? dto.getMetricName()
					: config.getString("webhook.message.metricName.nullreplace", "empty_value"));
			dto.setMetricThreshold(dto.getMetricThreshold() != null ? dto.getMetricThreshold()
					: config.getString("webhook.message.metricThreshold.nullreplace", "empty_value"));
			dto.setMetricValue(dto.getMetricValue() != null ? dto.getMetricValue()
					: config.getString("webhook.message.metricValue.nullreplace", "empty_value"));
			dto.setOname(dto.getOname() != null ? dto.getOname()
					: config.getString("webhook.message.oname.nullreplace", "empty_value"));
		}

		// @뒤로 네임스페이스가 붙어나오는 메시지 재가공
		String message = dto.getMessage();
		int idx = message.indexOf(config.getString("webhook.message.seperator", "@"));

		if (idx > 0) {
			String msgWithoutNamespace = message.substring(0, idx);
			String namespace = message.substring(idx + 1);

			dto.setOname(namespace);

			String prjName = dto.getProjectName();
			switch (prjName) {
			case "DVE_Kube":
			dto.setTitle("개발오픈시프트");
			break;
			case "DRE_Kube":
			dto.setTitle("DR오픈시프트");
			break;
			case "PRE_Kube":
			dto.setTitle("운영오픈시프트");
			break;
			}

			// // HPA메시지 판별
			// if (msgWithoutNamespace.contains("Scaled replication")) {

			// // 쌍따옴표 안의 podName 추출
			// String podPatternString = "\"([^\"]*)\"";
			// Pattern podPattern = Pattern.compile(podPatternString);
			// Matcher podMatcher = podPattern.matcher(msgWithoutNamespace);
			// String podName = "";
			// if (podMatcher.find()) {
			// podName = podMatcher.group(1);
			// }

			// // pod증감메시지 가공
			// Pattern countPattern = Pattern.compile("from (\\d+) to (\\d+)");
			// Matcher countMatcher = countPattern.matcher(msgWithoutNamespace);
			// int from = 0;
			// int to = 0;
			// if (countMatcher.find()) {
			// from = Integer.parseInt(countMatcher.group(1));
			// to = Integer.parseInt(countMatcher.group(2));
			// }
			// String countMsg = msgWithoutNamespace.replaceFirst(".* from (\\d+) to
			// (\\d+)", "$1개에서 $2개로 ");

			// String compString = "";
			// if (from > to) { // 감소한 경우
			// compString = "감소하였습니다.";
			// } else { // 증가한 경우
			// compString = "증가하였습니다.";
			// }

			// String finalMsg = prjName + " 프로젝트의 " + podName + " pod가 " + countMsg +
			// compString;
			// dto.setMessage(finalMsg);
			// } else {
			// dto.setMessage(msgWithoutNamespace);
			// }
			
			if (msgWithoutNamespace.contains("Scaled replication")) {
				String podName = msgWithoutNamespace.replaceAll("^.*\"([^\"]+)\".*$", "$1");
				String countMsg = msgWithoutNamespace.replaceAll("^.*from (\\d+) to (\\d+).*$", "$1개에서 $2개로");
				int from = Integer.parseInt(msgWithoutNamespace.replaceAll("^.*from (\\d+) to (\\d+).*$", "$1"));
				int to = Integer.parseInt(msgWithoutNamespace.replaceAll("^.*from (\\d+) to (\\d+).*$", "$2"));
				String compString = (from > to) ? "감소하였습니다." : "증가하였습니다.";
				String finalMsg = String.format("%s 프로젝트의 %s pod가 %s %s", prjName, podName, countMsg, compString);
				dto.setMessage(finalMsg);
			} else {
				dto.setMessage(msgWithoutNamespace);
			}
		}

		FilePrinter printer = new FilePrinter();
		printer.print(dto, path);
	}

}