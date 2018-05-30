package com.example.arch.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ArchConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArchConfigApplication.class, args);
	}
}
