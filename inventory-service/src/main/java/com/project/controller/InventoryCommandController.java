package com.project.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.domain.inventory.command.AddStockCommand;
import com.project.event_sourcing_core.service.CommandProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryCommandController {

    private final CommandProcessor commandProcessor;
    private final ObjectMapper objectMapper;

    @PostMapping("/add-stock")
    public ResponseEntity<JsonNode> addStock(@RequestBody JsonNode request) {

        UUID inventoryId = UUID.fromString(
                request.get("inventoryId").asText()
        );

        int quantity = request.get("quantity").asInt();

        var command = AddStockCommand.builder()
                .aggregateId(inventoryId)
                .quantity(quantity)
                .build();

        var result = commandProcessor.process(command);

        return ResponseEntity.ok(
                objectMapper.convertValue(result, new TypeReference<>() {})
        );
    }
}