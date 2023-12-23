package com.example.essentialcloud.transferbroker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequestMapping("/api/v1")
@EnableMethodSecurity
public class TransferBrokerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransferBrokerApplication.class, args);
	}

}
