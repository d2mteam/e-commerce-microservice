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
    private final com.project.infrastructure.query.JournalQueryService journalQueryService;

    @PostMapping("/create")
    public ResponseEntity<JsonNode> createInventory(@RequestBody JsonNode request) {
        UUID inventoryId = request.hasNonNull("inventoryId")
                ? UUID.fromString(request.get("inventoryId").asText())
                : UUID.randomUUID();
        String sku = request.get("sku").asText();
        int initialQuantity = request.get("initialQuantity").asInt(0);

        try {
            InventoryState state = inventoryGateway.createInventory(inventoryId, sku, initialQuantity)
                    .toCompletableFuture().join();
            return ResponseEntity.ok(objectMapper.convertValue(state, new TypeReference<>() {
            }));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                    objectMapper.createObjectNode().put("error", ex.getMessage())
            );
        }
    }

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

    @PostMapping("/reserve")
    public ResponseEntity<JsonNode> reserve(@RequestBody JsonNode request) {
        UUID inventoryId = UUID.fromString(request.get("inventoryId").asText());
        UUID orderId = UUID.fromString(request.get("orderId").asText());
        int quantity = request.get("quantity").asInt();
        String correlationId = request.hasNonNull("correlationId")
                ? request.get("correlationId").asText()
                : orderId.toString();

        try {
            InventoryState state = inventoryGateway.reserve(inventoryId, orderId, quantity, correlationId)
                    .toCompletableFuture().join();
            return ResponseEntity.ok(objectMapper.convertValue(state, new TypeReference<>() {
            }));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                    objectMapper.createObjectNode().put("error", ex.getMessage())
            );
        }
    }

    @PostMapping("/release")
    public ResponseEntity<JsonNode> release(@RequestBody JsonNode request) {
        UUID inventoryId = UUID.fromString(request.get("inventoryId").asText());
        UUID orderId = UUID.fromString(request.get("orderId").asText());

        try {
            InventoryState state = inventoryGateway.release(inventoryId, orderId)
                    .toCompletableFuture().join();
            return ResponseEntity.ok(objectMapper.convertValue(state, new TypeReference<>() {
            }));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                    objectMapper.createObjectNode().put("error", ex.getMessage())
            );
        }
    }

    @PostMapping("/cancel-reservation")
    public ResponseEntity<JsonNode> cancelReservation(@RequestBody JsonNode request) {
        UUID inventoryId = UUID.fromString(request.get("inventoryId").asText());
        UUID orderId = UUID.fromString(request.get("orderId").asText());

        try {
            InventoryState state = inventoryGateway.cancelReservation(inventoryId, orderId)
                    .toCompletableFuture().join();
            return ResponseEntity.ok(objectMapper.convertValue(state, new TypeReference<>() {
            }));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                    objectMapper.createObjectNode().put("error", ex.getMessage())
            );
        }
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<JsonNode> getInventory(@PathVariable("inventoryId") UUID inventoryId) {
        try {
            InventoryState state = inventoryGateway.getState(inventoryId)
                    .toCompletableFuture().join();
            return ResponseEntity.ok(objectMapper.convertValue(state, new TypeReference<>() {
            }));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                    objectMapper.createObjectNode().put("error", ex.getMessage())
            );
        }
    }

    @GetMapping("/{inventoryId}/events")
    public ResponseEntity<JsonNode> getInventoryEvents(@PathVariable("inventoryId") UUID inventoryId,
                                                       @RequestParam(name = "limit", required = false) Integer limit) {
        try {
            var events = journalQueryService.listEvents(inventoryId);
            if (limit != null && limit > 0 && limit < events.size()) {
                events = events.subList(events.size() - limit, events.size());
            }
            return ResponseEntity.ok(objectMapper.convertValue(events, new TypeReference<>() {
            }));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                    objectMapper.createObjectNode().put("error", ex.getMessage())
            );
        }
    }
}
