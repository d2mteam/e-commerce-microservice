// filepath: d:\KTPM\e-commerce-microservice\caller-service\src\main\java\org\example\controller\CallerController.java
package org.example.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/caller")
public class CallerController {

    private final WebClient webClient;

    public CallerController(WebClient webClient) {
        this.webClient = webClient;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ping")
    public String ping() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        String scopes = webClient
                .get()
                .uri("lb://callme-service/callme/ping")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return "Callme scopes: " + scopes;
    }
}
