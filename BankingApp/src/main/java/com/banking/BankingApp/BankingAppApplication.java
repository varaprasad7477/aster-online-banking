package com.banking.BankingApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BankingAppApplication {
	@Bean
	WebServerFactoryCustomizer<TomcatServletWebServerFactory> nio2Connector() {
		return factory -> factory.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");
	}

	public static void main(String[] args) {
		SpringApplication.run(BankingAppApplication.class, args);
	}

}
