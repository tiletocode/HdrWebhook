package com.hdr.whatap.webhook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class FilePrinter {

	public void print(WebhookDto dto, String path) throws IOException {

		Config config = Config.getConfig();

		String outFormat = config.getString("webhook.message.format",
				"D&D_Alert $metricName $pcode $level $metricValue $oid $title $message $uuid $metricThreshold $oname $projectName $status $time");

		outFormat = StringUtils.replace(outFormat, "#metricName", dto.getMetricName());
		outFormat = StringUtils.replace(outFormat, "#pcode", dto.getPcode());
		outFormat = StringUtils.replace(outFormat, "#level", dto.getLevel());
		outFormat = StringUtils.replace(outFormat, "#metricValue", dto.getMetricValue());
		outFormat = StringUtils.replace(outFormat, "#oid", dto.getOid());
		outFormat = StringUtils.replace(outFormat, "#title", dto.getTitle());
		outFormat = StringUtils.replace(outFormat, "#message", dto.getMessage());
		outFormat = StringUtils.replace(outFormat, "#uuid", dto.getUuid());
		outFormat = StringUtils.replace(outFormat, "#metricThreshold", dto.getMetricThreshold());
		outFormat = StringUtils.replace(outFormat, "#oname", dto.getOname());
		outFormat = StringUtils.replace(outFormat, "#projectName", dto.getProjectName());
		outFormat = StringUtils.replace(outFormat, "#status", dto.getStatus());
		outFormat = StringUtils.replace(outFormat, "#time", StringUtils.formatDate(dto.getTime(),
		Config.getConfig().getString("webhook.message.date.format", "yyyy-MM-dd HH:mm:ss")));

		//PrintWriter printWriter = new PrintWriter(new FileOutputStream(new File(path), true));
		PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(path), true), "UTF-8"));
		printWriter.println(outFormat);
		printWriter.close();
		
		printToday(outFormat, path);
	}
	
	private void printToday(String str, String path) throws IOException {
		Config config = Config.getConfig();

		long currentTimestamp = System.currentTimeMillis();
		String suffix = StringUtils.formatDate(currentTimestamp, config.getString("webhook.file.rolling.suffix", "yyyy-MM-dd"));
		String filePath = path + "." + suffix;
		
		PrintWriter printWriter = new PrintWriter(new FileOutputStream(new File(filePath), true));
		printWriter.println(str);
		printWriter.close();
	}

}
