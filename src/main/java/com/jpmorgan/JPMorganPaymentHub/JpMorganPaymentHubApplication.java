package com.jpmorgan.JPMorganPaymentHub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class JpMorganPaymentHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpMorganPaymentHubApplication.class, args);
	}

}
