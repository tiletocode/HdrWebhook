package com.hdr.whatap.webhook;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomContainer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
  public void customize(ConfigurableServletWebServerFactory factory){
	  Config config = Config.getConfig();
	  factory.setPort(config.getInt("webhook.server.port", 9091));
  }
}