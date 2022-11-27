package com.hdr.whatap.webhook;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Config {

	private Properties props = new Properties();
	private static Config instance = new Config();
	private Config() {
		//System.getProperty(): java -jar ...jar -Dconfig.path= x/x.conf
		String propsPath = System.getProperty("config.path", "config/config.properties");
		try {
			InputStream is = new FileInputStream(propsPath);
			props.load(is);
		} catch (Exception e){
			
			log.error("Failed to read the config file", e);
//			System.exit(1);
		}
		
	}
	
	public static Config getConfig() {
		return instance;
	}
	
	public String getString(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}
	
	public int getInt(String key, int defaultValue) {
		return Integer.parseInt(props.getProperty(key, Integer.toString(defaultValue)));
	}

	
	@Override
	public String toString() {
		return props.toString();
	}
}