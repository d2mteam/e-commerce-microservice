package com.project.application.service;

import com.project.application.integration.impl.ProductReleaseReply;
import com.project.application.integration.impl.ProductReserveReply;
import com.project.domain.order.aggregate.OrderAggregate;
import com.project.domain.order.command.ConfirmStockCommand;
import com.project.domain.order.command.OutOfStockCommand;
import com.project.event_sourcing_core.domain.Aggregate;
import com.project.event_sourcing_core.service.AggregateStore;
import com.project.event_sourcing_core.service.CommandProcessor;
import com.project.infrastructure.jpa.entity.OrderSagaTracker;
import com.project.infrastructure.jpa.repository.OrderSagaTrackerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaOrchestrator {

    private final AggregateStore aggregateStore;
    private final CommandProcessor commandProcessor;
    private final OrderSagaTrackerRepository trackerRepository;

    @Transactional
    @EventListener
    public void handle(ProductReserveReply reply) {
        OrderSagaTracker tracker = trackerRepository
                .findByOrderIdAndStage(reply.getOrderId(), OrderSagaTracker.Stage.RESERVATION)
                .orElseGet(() -> buildReservationTracker(reply.getOrderId()));

        if (tracker == null) {
            log.warn("Skip reservation reply for unknown order {}", reply.getOrderId());
            return;
        }

        if (!tracker.recordReservation(reply)) {
            return;
        }

        boolean completed = tracker.markCompletedIfReady();
        trackerRepository.save(tracker);

        if (completed) {
            if (tracker.isSuccessful()) {
                commandProcessor.process(ConfirmStockCommand.builder()
                        .aggregateId(reply.getOrderId())
                        .userId(tracker.getUserId())
                        .inventoryId(tracker.confirmationReferenceOrDefault(reply.getOrderId()))
                        .build());
            } else {
                String reason = String.join("; ", tracker.getFailureReasons());
                if (reason.isBlank()) {
                    reason = "RESERVATION_FAILED";
                }
                commandProcessor.process(OutOfStockCommand.builder()
                        .aggregateId(reply.getOrderId())
                        .reason(reason)
                        .build());
            }
        }
    }

    @Transactional
    @EventListener
    public void handle(ProductReleaseReply reply) {
        OrderSagaTracker tracker = trackerRepository
                .findByOrderIdAndStage(reply.getOrderId(), OrderSagaTracker.Stage.RELEASE)
                .orElseGet(() -> buildReleaseTracker(reply.getOrderId()));

        if (tracker == null) {
            log.warn("Skip release reply for unknown order {}", reply.getOrderId());
            return;
        }

        if (!tracker.recordRelease(reply)) {
            return;
        }

        boolean completed = tracker.markCompletedIfReady();
        trackerRepository.save(tracker);

        if (completed) {
            if (tracker.isSuccessful()) {
                log.info("Release completed for order {}", reply.getOrderId());
            } else {
                log.warn("Release completed with failures for order {}: {}",
                        reply.getOrderId(), String.join("; ", tracker.getFailureReasons()));
            }
        }
    }

    private OrderSagaTracker buildReservationTracker(UUID orderId) {
        OrderAggregate aggregate = loadOrderAggregate(orderId);
        if (aggregate == null) {
            return null;
        }
        int expected = Math.max(1, aggregate.getOrderDetails().size());
        return OrderSagaTracker.newReservation(aggregate.getAggregateId(), aggregate.getUserId(), expected);
    }

    private OrderSagaTracker buildReleaseTracker(UUID orderId) {
        OrderAggregate aggregate = loadOrderAggregate(orderId);
        if (aggregate == null) {
            return null;
        }
        int expected = Math.max(1, aggregate.getOrderDetails().size());
        return OrderSagaTracker.newRelease(aggregate.getAggregateId(), expected);
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
}
