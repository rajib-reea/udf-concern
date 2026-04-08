package com.udf.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UdfMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UdfMicroserviceApplication.class, args);
    }
}