package com.project.controller;

import com.project.akka.inventory.InventoryState;
import com.project.infrastructure.jpa.entity.InventorySummary;
import com.project.infrastructure.jpa.repository.InventorySummaryRepository;
import com.project.infrastructure.query.JournalQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory-query")
@RequiredArgsConstructor
public class InventoryQueryController {

    private final InventorySummaryRepository repository;
    private final JournalQueryService journalQueryService;

    /**
     * Query using materialized view (fast - pre-computed)
     */
    @GetMapping("/{inventoryId}")
    public ResponseEntity<InventorySummary> getInventorySummary(@PathVariable UUID inventoryId) {
        return repository.findById(inventoryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Query using event replay (slow - replays all events from journal)
     * Useful for benchmarking event sourcing vs materialized views
     */
    @GetMapping("/replay/{inventoryId}")
    public ResponseEntity<InventoryState> getInventoryByReplay(@PathVariable UUID inventoryId) {
        return journalQueryService.replayToState(inventoryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get event statistics for an inventory (count, breakdown by type)
     */
    @GetMapping("/stats/{inventoryId}")
    public ResponseEntity<Map<String, Object>> getEventStats(@PathVariable UUID inventoryId) {
        Map<String, Object> stats = journalQueryService.getEventStats(inventoryId);
        if ((int) stats.get("totalEvents") == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }
}
