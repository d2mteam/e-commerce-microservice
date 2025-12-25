package com.project.infrastructure.projection;

import akka.actor.typed.ActorSystem;
import akka.persistence.jdbc.query.javadsl.JdbcReadJournal;
import akka.persistence.query.Offset;
import akka.persistence.query.PersistenceQuery;
import akka.stream.Materializer;
import akka.stream.SystemMaterializer;
import com.project.akka.inventory.InventoryEvent;
import com.project.infrastructure.jpa.entity.InventorySummary;
import com.project.infrastructure.jpa.repository.InventorySummaryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryReadModelProjection {

    private final ActorSystem<Void> actorSystem;
    private final InventorySummaryRepository repository;

    @PostConstruct
    public void init() {
        Materializer mat = SystemMaterializer.get(actorSystem).materializer();
        JdbcReadJournal readJournal = PersistenceQuery.get(actorSystem.classicSystem())
                .getReadJournalFor(JdbcReadJournal.class, JdbcReadJournal.Identifier());

        readJournal.eventsByTag("inventory", Offset.noOffset())
                .runForeach(envelope -> {
                    log.info("Projection received event: {} | PersistenceId: {}", envelope.event().getClass().getSimpleName(), envelope.persistenceId());
                    if (envelope.event() instanceof InventoryEvent event) {
                        try {
                            // Extract UUID from persistenceId "Inventory|UUID"
                            String[] parts = envelope.persistenceId().split("\\|");
                            if (parts.length == 2) {
                                UUID inventoryId = UUID.fromString(parts[1]);
                                updateReadModel(inventoryId, event);
                            }
                        } catch (Exception ex) {
                            log.warn("Failed to parse inventoryId from persistenceId: {}", envelope.persistenceId());
                        }
                    }
                }, mat);
        
        log.info("Started Inventory Read Model Projection...");
    }

    private void updateReadModel(UUID inventoryId, InventoryEvent event) {
        try {
            switch (event) {
                case InventoryEvent.InventoryCreated e -> {
                    InventorySummary summary = InventorySummary.builder()
                            .inventoryId(inventoryId)
                            .sku(e.sku())
                            .availableQuantity(e.initialQuantity())
                            .reservedQuantity(0)
                            .lastUpdated(e.createdAt())
                            .build();
                    repository.save(summary);
                    log.debug("Created summary for {}", inventoryId);
                }
                case InventoryEvent.StockAdded e -> {
                    repository.findById(inventoryId).ifPresent(summary -> {
                        summary.setAvailableQuantity(summary.getAvailableQuantity() + e.quantity());
                        summary.setLastUpdated(e.addedAt());
                        repository.save(summary);
                    });
                }
                case InventoryEvent.StockReserved e -> {
                    repository.findById(inventoryId).ifPresent(summary -> {
                        summary.setAvailableQuantity(summary.getAvailableQuantity() - e.quantity());
                        summary.setReservedQuantity(summary.getReservedQuantity() + e.quantity());
                        summary.setLastUpdated(e.reservedAt());
                        repository.save(summary);
                    });
                }
                case InventoryEvent.ReservationCancelled e -> {
                     // Note: Event does not contain quantity, so we cannot update numeric values accurately
                     // without querying the aggregate or enhancing the event.
                     // For now, we update the timestamp to show activity.
                     repository.findById(inventoryId).ifPresent(summary -> {
                        summary.setLastUpdated(e.cancelledAt());
                        repository.save(summary);
                    });
                     log.debug("Reservation cancelled event for {}", inventoryId);
                }
                case InventoryEvent.StockReleased e -> {
                     // Note: Similarly, StockReleased needs quantity to deduct from reservedQuantity.
                     repository.findById(inventoryId).ifPresent(summary -> {
                        summary.setLastUpdated(e.releasedAt());
                        repository.save(summary);
                    });
                }
            }
        } catch (Exception ex) {
            log.error("Error updating read model for inventory {}", inventoryId, ex);
        }
    }
}