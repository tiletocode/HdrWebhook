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
			String messageFix = message.substring(0, idx);
			String namespace = message.substring(idx + 1);
			
			if (messageFix.contains("Scaled") == true) {
				dto.setMessage(messageFix.replace("replica set ", ""));
			} else {
				dto.setMessage(messageFix);
			}
			
			dto.setOname(namespace);
			dto.setTitle(dto.getProjectName());
		}
		
		FilePrinter printer = new FilePrinter();
		printer.print(dto, path);
	}

}
