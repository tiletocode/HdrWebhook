
package com.hdr.whatap.webhook;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class HdrWebhookApplication {

	
	@Autowired
    AutowireCapableBeanFactory beanFactory; 
	
	@Bean
	public ServletRegistrationBean<Receiver> ReceiverServletRegistrationBean() {
		
				
		Config config = Config.getConfig();
		log.info("Config file \n" + config.toString());
		
		
		ServletRegistrationBean srb = new ServletRegistrationBean();
		final Receiver servlet = new Receiver();
		beanFactory.autowireBean(servlet); // <--- The most important part
		srb.setServlet(servlet);
		srb.setUrlMappings(Arrays.asList(config.getString("webhook.server.uri", "/webhook")));
		return srb;
	}

	public static void main(String[] args) {
		SpringApplication.run(HdrWebhookApplication.class, args);
	}

}