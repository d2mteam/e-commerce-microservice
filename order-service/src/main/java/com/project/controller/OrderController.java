package com.project.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.domain.order.command.CancelOrderCommand;
import com.project.domain.order.command.CreateOrderCommand;
import com.project.event_sourcing_core.domain.command.Command;
import com.project.event_sourcing_core.service.CommandProcessor;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final ObjectMapper objectMapper;
    private final CommandProcessor commandProcessor;

    @PostMapping
    public ResponseEntity<JsonNode> placeOrder(@RequestBody JsonNode request) {
        JsonNode orderDetails = request.get("orderDetails");
        UUID orderId = UUID.randomUUID();
        Command command = CreateOrderCommand.builder()
                .userId(UUID.fromString(request.get("userId").asText()))
                .orderDetails(objectMapper.convertValue(orderDetails, new TypeReference<>() {
                }))
                .aggregateId(orderId)
                .build();
        var order = commandProcessor.process(command);
        return ResponseEntity.ok().body(objectMapper.convertValue(order, new TypeReference<>() {
        }));
    }

    @DeleteMapping
    public ResponseEntity<JsonNode> cancelOrder(@RequestBody JsonNode request) {
        Command command = CancelOrderCommand.builder()
                .userId(UUID.fromString(request.get("userId").asText()))
                .reason(request.get("reason").asText())
                .aggregateId(UUID.fromString(request.get("aggregateId").asText()))
                .build();
        var order = commandProcessor.process(command);
        return ResponseEntity.ok().body(objectMapper.convertValue(order, new TypeReference<>() {
        }));
    }
}
