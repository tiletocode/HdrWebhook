package com.hdr.whatap.webhook;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Scheduler {
    
	@Scheduled(cron = "0 0 0 * * ?")	
	public void cronJob() {
		Config config = Config.getConfig();
		try {
			log.info("[File Rolling CronJob] is running");
			String filePath_app = config.getString("webhook.app.file.path", "out/app/whatap_out.txt");
			String filePath_infra = config.getString("webhook.infra.file.path", "out/infra/whatap_out.txt");
			String filePath_db = config.getString("webhook.db.file.path", "out/db/whatap_out.txt");
			String filePath_kube = config.getString("webhook.kube.file.path", "out/kube/whatap_out.txt");
			
			truncateFiles(filePath_app, filePath_infra, filePath_db, filePath_kube);
			
			long currentTimestamp = System.currentTimeMillis();
			long oneDay = 1000L * 60 * 60 * 24;
			int targetSize = config.getInt("webhook.file.rolling.size", 10);
			if(targetSize < 2) {
				log.warn("webhook.file.rolling.size set to default value : 2");
				targetSize = 2;
			}
			
			long targetTimestamp = currentTimestamp - (oneDay * targetSize);
			String suffix = StringUtils.formatDate(targetTimestamp, config.getString("webhook.file.rolling.suffix", "yyyy-MM-dd"));
			
			String targetFilePath_app = config.getString("webhook.app.file.path", "out/app/whatap_out.txt") + "." + suffix;
			String targetFilePath_infra = config.getString("webhook.infra.file.path", "out/infra/whatap_out.txt") + "." + suffix;
			String targetFilePath_db = config.getString("webhook.db.file.path", "out/db/whatap_out.txt") + "." + suffix;
			String targetFilePath_kube = config.getString("webhook.kube.file.path", "out/kube/whatap_out.txt") + "." + suffix;
			deleteOldFiles(targetFilePath_app, targetFilePath_infra, targetFilePath_db, targetFilePath_kube);
			
			log.info("[File Rolling CronJob] finished");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private void truncateFiles(String... paths) throws IOException {
		for(String path : paths) {
			File file = new File(path);
			if(file.exists()) {
				Files.write(Paths.get(path), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
			}
		}
	}

	private void deleteOldFiles(String... paths) {
		
		for(String path : paths) {
			File file = new File(path);
			if(file.exists()) {
				file.delete();
			}

		}
	}
	
}
