package com.project.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.akka.inventory.InventoryGateway;
import com.project.akka.inventory.InventoryState;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryCommandController {

    private final InventoryGateway inventoryGateway;
    private final ObjectMapper objectMapper;

    @PostMapping("/add-stock")
    public ResponseEntity<JsonNode> addStock(@RequestBody JsonNode request) {
        UUID inventoryId = UUID.fromString(request.get("inventoryId").asText());
        int quantity = request.get("quantity").asInt();

        try {
            InventoryState state = inventoryGateway.addStock(inventoryId, quantity).toCompletableFuture().join();
            return ResponseEntity.ok(objectMapper.convertValue(state, new TypeReference<>() {
            }));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                    objectMapper.createObjectNode().put("error", ex.getMessage())
            );
        }
    }
}
