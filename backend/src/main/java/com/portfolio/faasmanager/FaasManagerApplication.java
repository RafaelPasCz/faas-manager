package com.portfolio.faasmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing

public class FaasManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FaasManagerApplication.class, args);
	}

}
