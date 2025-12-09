package com.project.application.service;

import com.project.application.integration.impl.ProductReleaseReply;
import com.project.application.integration.impl.ProductReserveReply;
import com.project.domain.order.aggregate.OrderAggregate;
import com.project.domain.order.command.ConfirmStockCommand;
import com.project.domain.order.command.OutOfStockCommand;
import com.project.event_sourcing_core.domain.Aggregate;
import com.project.event_sourcing_core.service.AggregateStore;
import com.project.event_sourcing_core.service.CommandProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaOrchestrator {

    private final AggregateStore aggregateStore;
    private final CommandProcessor commandProcessor;

    private final ConcurrentHashMap<UUID, ReservationTracker> reservationTrackers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ReleaseTracker> releaseTrackers = new ConcurrentHashMap<>();

    @EventListener
    public void handle(ProductReserveReply reply) {
        ReservationTracker tracker = reservationTrackers.get(reply.getOrderId());
        if (tracker == null) {
            tracker = ReservationTracker.from(loadOrderAggregate(reply.getOrderId()));
            if (tracker == null) {
                log.warn("Skip reservation reply for unknown order {}", reply.getOrderId());
                return;
            }
            reservationTrackers.put(reply.getOrderId(), tracker);
        }


        if (!tracker.record(reply)) {
            return;
        }

        if (tracker.complete()) {
            reservationTrackers.remove(reply.getOrderId());
            if (tracker.isSuccessful()) {
                commandProcessor.process(ConfirmStockCommand.builder()
                        .aggregateId(reply.getOrderId())
                        .userId(tracker.userId())
                        .inventoryId(tracker.confirmationReferenceId())
                        .build());
            } else {
                String reason = tracker.failureSummary();
                if (reason == null || reason.isBlank()) {
                    reason = "RESERVATION_FAILED";
                }
                commandProcessor.process(OutOfStockCommand.builder()
                        .aggregateId(reply.getOrderId())
                        .reason(reason)
                        .build());
            }
        }
    }

    @EventListener
    public void handle(ProductReleaseReply reply) {
        ReleaseTracker tracker = releaseTrackers.get(reply.getOrderId());
        if (tracker == null) {
            tracker = ReleaseTracker.from(loadOrderAggregate(reply.getOrderId()));
            if (tracker == null) {
                log.warn("Skip release reply for unknown order {}", reply.getOrderId());
                return;
            }
            releaseTrackers.put(reply.getOrderId(), tracker);
        }

        if (!tracker.record(reply)) {
            return;
        }

        if (tracker.complete()) {
            releaseTrackers.remove(reply.getOrderId());
            if (tracker.isSuccessful()) {
                log.info("Release completed for order {}", reply.getOrderId());
            } else {
                log.warn("Release completed with failures for order {}: {}",
                        reply.getOrderId(), tracker.failureSummary());
            }
        }
    }

    private OrderAggregate loadOrderAggregate(UUID orderId) {
        try {
            Aggregate aggregate = aggregateStore.readAggregate(OrderAggregate.class.getSimpleName(), orderId);
            return (OrderAggregate) aggregate;
        } catch (Exception ex) {
            log.error("Failed to load order aggregate {}: {}", orderId, ex.getMessage(), ex);
            return null;
        }
    }

    private record ReservationTracker(UUID orderId,
                                      UUID userId,
                                      int expectedReplies,
                                      Set<UUID> processedProducts,
                                      AtomicInteger received,
                                      AtomicBoolean finalized,
                                      List<String> failureReasons,
                                      Holder confirmationReference) {

        static ReservationTracker from(OrderAggregate aggregate) {
            if (aggregate == null) {
                return null;
            }
            int expected = Math.max(1, aggregate.getOrderDetails().size());
            return new ReservationTracker(
                    aggregate.getAggregateId(),
                    aggregate.getUserId(),
                    expected,
                    ConcurrentHashMap.newKeySet(),
                    new AtomicInteger(0),
                    new AtomicBoolean(false),
                    Collections.synchronizedList(new ArrayList<>()),
                    new Holder()
            );
        }

        boolean record(ProductReserveReply reply) {
            if (!processedProducts.add(reply.getProductId())) {
                return false;
            }
            if (reply.getResult() == ProductReserveReply.Result.FAILURE) {
                String reason = reply.getReason() == null ? "UNKNOWN_REASON" : reply.getReason();
                failureReasons.add(reason);
            } else if (confirmationReference.value == null) {
                confirmationReference.value = reply.getProductId();
            }
            received.incrementAndGet();
            return true;
        }

        boolean complete() {
            return !finalized.get()
                    && received.get() >= expectedReplies
                    && finalized.compareAndSet(false, true);
        }

        boolean isSuccessful() {
            return failureReasons.isEmpty();
        }

        public UUID userId() {
            return userId;
        }

        UUID confirmationReferenceId() {
            return confirmationReference.value != null ? confirmationReference.value : orderId;
        }

        String failureSummary() {
            return String.join("; ", failureReasons);
        }
    }

    private record ReleaseTracker(UUID orderId,
                                  int expectedReplies,
                                  Set<UUID> processedProducts,
                                  AtomicInteger received,
                                  AtomicBoolean finalized,
                                  List<String> failureReasons) {

        static ReleaseTracker from(OrderAggregate aggregate) {
            if (aggregate == null) {
                return null;
            }
            int expected = Math.max(1, aggregate.getOrderDetails().size());
            return new ReleaseTracker(
                    aggregate.getAggregateId(),
                    expected,
                    ConcurrentHashMap.newKeySet(),
                    new AtomicInteger(0),
                    new AtomicBoolean(false),
                    Collections.synchronizedList(new ArrayList<>())
            );
        }

        boolean record(ProductReleaseReply reply) {
            if (!processedProducts.add(reply.getProductId())) {
                return false;
            }
            if (reply.getResult() == ProductReleaseReply.Result.FAILURE) {
                String reason = reply.getReason() == null ? "UNKNOWN_REASON" : reply.getReason();
                failureReasons.add(reason);
            }
            received.incrementAndGet();
            return true;
        }

        boolean complete() {
            return !finalized.get()
                    && received.get() >= expectedReplies
                    && finalized.compareAndSet(false, true);
        }

        boolean isSuccessful() {
            return failureReasons.isEmpty();
        }

        String failureSummary() {
            return String.join("; ", failureReasons);
        }
    }

    private static class Holder {
        private UUID value;
    }
}
