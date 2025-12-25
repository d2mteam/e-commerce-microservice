package com.project.infrastructure.query;

import akka.actor.typed.ActorSystem;
import akka.persistence.jdbc.query.javadsl.JdbcReadJournal;
import akka.persistence.query.EventEnvelope;
import akka.persistence.query.PersistenceQuery;
import akka.stream.Materializer;
import akka.stream.SystemMaterializer;
import akka.stream.javadsl.Sink;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.akka.inventory.InventoryEvent;
import com.project.akka.inventory.InventoryState;
import com.project.akka.inventory.InventoryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class JournalQueryService {

    private final ActorSystem<Void> actorSystem;
    private final ObjectMapper objectMapper;

    public List<Map<String, Object>> listEvents(UUID aggregateId) {
        String pid = "Inventory|" + aggregateId;
        Materializer mat = SystemMaterializer.get(actorSystem).materializer();
        JdbcReadJournal readJournal = PersistenceQuery.get(actorSystem.classicSystem())
                .getReadJournalFor(JdbcReadJournal.class, JdbcReadJournal.Identifier());

        try {
            // currentEventsByPersistenceId kết thúc khi hết sự kiện, tránh treo vô hạn
            List<EventEnvelope> envelopes = readJournal.currentEventsByPersistenceId(pid, 0, Long.MAX_VALUE)
                    .runWith(Sink.seq(), mat)
                    .toCompletableFuture()
                    .get();

            return envelopes.stream()
                    .map(env -> {
                        Map<String, Object> m = new java.util.LinkedHashMap<>();
                        m.put("persistenceId", env.persistenceId());
                        m.put("sequenceNumber", env.sequenceNr());
                        m.put("timestamp", Instant.ofEpochMilli(env.timestamp()).toString());
                        m.put("event", objectMapper.valueToTree(env.event()));
                        m.put("eventType", env.event().getClass().getName());
                        return m;
                    })
                    .toList();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to load events", e);
        }
    }

    /**
     * Count total events for an inventory
     */
    public long countEvents(UUID inventoryId) {
        String pid = "Inventory|" + inventoryId;
        Materializer mat = SystemMaterializer.get(actorSystem).materializer();
        JdbcReadJournal readJournal = PersistenceQuery.get(actorSystem.classicSystem())
                .getReadJournalFor(JdbcReadJournal.class, JdbcReadJournal.Identifier());

        try {
            return readJournal.currentEventsByPersistenceId(pid, 0, Long.MAX_VALUE)
                    .runWith(Sink.seq(), mat)
                    .toCompletableFuture()
                    .get()
                    .size();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to count events", e);
        }
    }

    /**
     * Get event statistics for an inventory
     */
    public Map<String, Object> getEventStats(UUID inventoryId) {
        List<Map<String, Object>> events = listEvents(inventoryId);

        Map<String, Long> eventTypeCounts = new HashMap<>();
        for (Map<String, Object> event : events) {
            String eventType = (String) event.get("eventType");
            String simpleName = eventType.substring(eventType.lastIndexOf('$') + 1);
            eventTypeCounts.merge(simpleName, 1L, Long::sum);
        }

        Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("inventoryId", inventoryId.toString());
        stats.put("totalEvents", events.size());
        stats.put("eventBreakdown", eventTypeCounts);

        if (!events.isEmpty()) {
            stats.put("firstEventAt", events.get(0).get("timestamp"));
            stats.put("lastEventAt", events.get(events.size() - 1).get("timestamp"));
        }

        return stats;
    }

    /**
     * Replay all events from journal to reconstruct state.
     * This forces a full event replay every time - no caching, no snapshots.
     * Useful for benchmarking event sourcing vs materialized views.
     */
    public Optional<InventoryState> replayToState(UUID inventoryId) {
        String pid = "Inventory|" + inventoryId;
        Materializer mat = SystemMaterializer.get(actorSystem).materializer();
        JdbcReadJournal readJournal = PersistenceQuery.get(actorSystem.classicSystem())
                .getReadJournalFor(JdbcReadJournal.class, JdbcReadJournal.Identifier());

        try {
            List<EventEnvelope> envelopes = readJournal.currentEventsByPersistenceId(pid, 0, Long.MAX_VALUE)
                    .runWith(Sink.seq(), mat)
                    .toCompletableFuture()
                    .get();

            if (envelopes.isEmpty()) {
                return Optional.empty();
            }

            // Start with empty state and apply each event
            InventoryState state = InventoryState.empty(inventoryId);
            for (EventEnvelope envelope : envelopes) {
                state = applyEvent(state, (InventoryEvent) envelope.event());
            }

            return Optional.of(state);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to replay events", e);
        }
    }

    private InventoryState applyEvent(InventoryState state, InventoryEvent event) {
        return switch (event) {
            case InventoryEvent.InventoryCreated e -> state.toBuilder()
                    .sku(e.sku())
                    .availableQuantity(e.initialQuantity())
                    .reservedQuantity(0)
                    .status(InventoryStatus.ACTIVE)
                    .createdAt(e.createdAt())
                    .updatedAt(e.createdAt())
                    .build();
            case InventoryEvent.StockAdded e -> state.toBuilder()
                    .availableQuantity(state.availableQuantity() + e.quantity())
                    .updatedAt(e.addedAt())
                    .build();
            case InventoryEvent.StockReserved e -> {
                Map<UUID, Integer> reservations = new HashMap<>(
                        state.reservations() == null ? Map.of() : state.reservations());
                reservations.put(e.orderId(), e.quantity());
                yield state.toBuilder()
                        .availableQuantity(state.availableQuantity() - e.quantity())
                        .reservedQuantity(state.reservedQuantity() + e.quantity())
                        .reservations(reservations)
                        .updatedAt(e.reservedAt())
                        .build();
            }
            case InventoryEvent.ReservationCancelled e -> {
                int qty = state.reservations() != null ? state.reservations().getOrDefault(e.orderId(), 0) : 0;
                Map<UUID, Integer> reservations = new HashMap<>(
                        state.reservations() == null ? Map.of() : state.reservations());
                reservations.remove(e.orderId());
                yield state.toBuilder()
                        .availableQuantity(state.availableQuantity() + qty)
                        .reservedQuantity(Math.max(0, state.reservedQuantity() - qty))
                        .reservations(reservations)
                        .updatedAt(e.cancelledAt())
                        .build();
            }
            case InventoryEvent.StockReleased e -> {
                int qty = state.reservations() != null ? state.reservations().getOrDefault(e.orderId(), 0) : 0;
                Map<UUID, Integer> reservations = new HashMap<>(
                        state.reservations() == null ? Map.of() : state.reservations());
                reservations.remove(e.orderId());
                yield state.toBuilder()
                        .reservedQuantity(Math.max(0, state.reservedQuantity() - qty))
                        .reservations(reservations)
                        .updatedAt(e.releasedAt())
                        .build();
            }
        };
    }
}
