package com.project.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.akka.order.OrderGateway;
import com.project.akka.order.OrderState;
import com.project.domain.order.aggregate.vo.OrderDetail;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final ObjectMapper objectMapper;
    private final OrderGateway orderGateway;

    @PostMapping
    public ResponseEntity<JsonNode> placeOrder(@RequestBody JsonNode request) {
        JsonNode orderDetails = request.get("orderDetails");
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.fromString(request.get("userId").asText());
        try {
            var details = objectMapper.convertValue(orderDetails, new TypeReference<java.util.List<OrderDetail>>() {
            });
            OrderState order = orderGateway.createOrder(orderId, userId, details).toCompletableFuture().join();
            return ResponseEntity.ok().body(objectMapper.convertValue(order, new TypeReference<>() {
            }));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                    objectMapper.createObjectNode().put("error", ex.getMessage())
            );
        }
    }

    @DeleteMapping
    public ResponseEntity<JsonNode> cancelOrder(@RequestBody JsonNode request) {
        UUID orderId = UUID.fromString(request.get("aggregateId").asText());
        UUID userId = UUID.fromString(request.get("userId").asText());
        String reason = request.get("reason").asText();

        try {
            OrderState order = orderGateway.cancelOrder(orderId, userId, reason).toCompletableFuture().join();
            return ResponseEntity.ok().body(objectMapper.convertValue(order, new TypeReference<>() {
            }));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                    objectMapper.createObjectNode().put("error", ex.getMessage())
            );
        }
    }
}
