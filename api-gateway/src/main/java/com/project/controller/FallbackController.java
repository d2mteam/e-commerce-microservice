package com.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/product-service")
    public Mono<ResponseEntity<Map<String, Object>>> productServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "status", "error",
                "service", "product-service",
                "message", "Product service is temporarily unavailable",
                "suggestion", "Please try again in a few seconds",
                "timestamp", Instant.now().toString()
        )));
    }

    @GetMapping("/order-service")
    public Mono<ResponseEntity<Map<String, Object>>> orderServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "status", "error",
                "service", "order-service",
                "message", "Order service is temporarily unavailable",
                "suggestion", "Your order has not been processed. Please try again later",
                "timestamp", Instant.now().toString()
        )));
    }

    @GetMapping("/inventory-service")
    public Mono<ResponseEntity<Map<String, Object>>> inventoryServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "status", "error",
                "service", "inventory-service",
                "message", "Inventory service is temporarily unavailable",
                "suggestion", "Stock information may be outdated. Please refresh later",
                "timestamp", Instant.now().toString()
        )));
    }

    @GetMapping("/service-failure")
    public Mono<ResponseEntity<Map<String, Object>>> genericFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "status", "error",
                "service", "unknown",
                "message", "Service is currently unavailable",
                "suggestion", "Please try again later",
                "timestamp", Instant.now().toString()
        )));
    }
}
