package com.project.orderservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.orderservice.domain.OrderDetail;
import com.project.orderservice.domain.command.CancelOrderCommand;
import com.project.orderservice.domain.command.CreateOrderCommand;
import com.project.orderservice.domain.command.StartShippingCommand;
import com.project.orderservice.event_sourcing_core.domain.Aggregate;
import com.project.orderservice.event_sourcing_core.domain.command.Command;
import com.project.orderservice.event_sourcing_core.service.CommandProcessor;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
public class OrderController {
    private final ObjectMapper mapper;
    private final CommandProcessor commandProcessor;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody JsonNode request) {
        JsonNode usersNode = request.get("orderDetails");
        UUID orderId = UUID.randomUUID();
        Command command = CreateOrderCommand.builder()
                .userId(UUID.fromString(request.get("userId").asText()))
                .orderDetails(mapper.convertValue(usersNode, new TypeReference<>() {}))
                .aggregateId(orderId)
                .build();
        return ResponseEntity.ok().body(commandProcessor.process(command));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable UUID orderId, @RequestBody JsonNode request) {
        Command command = CancelOrderCommand.builder()
                .aggregateId(orderId)
                .userId(UUID.fromString(request.get("userId").asText()))
                .reason(request.get("reason").asText())
                .build();
        return ResponseEntity.ok().body(commandProcessor.process(command));
    }

//    @PostMapping("/{orderId}/ship")
//    public ResponseEntity<?> startShipping(@PathVariable UUID orderId, @RequestBody JsonNode request) {
//        return null;
//    }
//
//    @PostMapping("/{orderId}/delivered")
//    public ResponseEntity<?> markDelivered(@PathVariable UUID orderId, @RequestBody JsonNode request) {
//        return null;
//    }
//
//    @PostMapping("/{orderId}/paid")
//    public ResponseEntity<?> markPaid(@PathVariable UUID orderId, @RequestBody JsonNode request) {
//        return null;
//    }
//
//    @PostMapping("/{orderId}/confirm-stock")
//    public ResponseEntity<?> confirmStock(@PathVariable UUID orderId, @RequestBody JsonNode request) {
//        return null;
//    }
//
//    @PostMapping("/{orderId}/outofstock")
//    public ResponseEntity<?> markOutOfStock(@PathVariable UUID orderId, @RequestBody JsonNode request) {
//        return null;
//    }

    @PostMapping("/events")
    public ResponseEntity<?> receiveExternalEvent(@RequestBody JsonNode eventPayload) {
        return null;
    }
}
