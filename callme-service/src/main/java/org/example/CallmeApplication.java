package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CallmeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CallmeApplication.class, args);
    }

}