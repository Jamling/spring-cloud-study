package com.example.arch.config.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ArchConfigTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchConfigTestApplication.class, args);
    }
}
