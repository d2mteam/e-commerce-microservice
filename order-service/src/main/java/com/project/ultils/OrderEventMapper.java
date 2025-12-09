package com.project.ultils;

import com.project.application.integration.IntegrationEvent;
import com.project.application.integration.impl.ProductReleaseRequest;
import com.project.application.integration.impl.ProductReserveRequest;
import com.project.application.integration.mapper.DomainToIntegrationEventMapper;
import com.project.domain.order.aggregate.OrderAggregate;
import com.project.domain.order.event.OrderCreatedEvent;
import com.project.domain.order.event.OrderCancelledEvent;
import com.project.event_sourcing_core.domain.event.Event;
import com.project.event_sourcing_core.service.AggregateStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventMapper implements DomainToIntegrationEventMapper<Event> {

    private final AggregateStore aggregateStore;

    @Override
    public boolean supports(Class<?> eventClass) {
        return eventClass == OrderCreatedEvent.class ||
                eventClass == OrderCancelledEvent.class;
    }

    @Override
    public List<IntegrationEvent> map(Event event) {

        List<IntegrationEvent> result = new ArrayList<>();

        if (event instanceof OrderCreatedEvent created) {
            String correlationId = created.getAggregateId().toString();

            created.getOrderDetails().forEach(detail -> {
                result.add(ProductReserveRequest.builder()
                        .orderId(created.getAggregateId())
                        .productId(detail.getProductId())
                        .quantity(detail.getQuantity())
                        .correlationId(correlationId)
                        .build());
            });
        }

        if (event instanceof OrderCancelledEvent cancelled) {
            String correlationId = cancelled.getAggregateId().toString();

            OrderAggregate aggregate = loadAggregate(cancelled.getAggregateId());
            if (aggregate != null) {
                aggregate.getOrderDetails().forEach(detail -> result.add(ProductReleaseRequest.builder()
                        .orderId(cancelled.getAggregateId())
                        .productId(detail.getProductId())
                        .correlationId(correlationId)
                        .build()));
            }
        }

        return result;
    }

    private OrderAggregate loadAggregate(UUID aggregateId) {
        try {
            return (OrderAggregate) aggregateStore.readAggregate(OrderAggregate.class.getSimpleName(), aggregateId);
        } catch (Exception ex) {
            log.error("Failed to load aggregate {} for integration mapping: {}", aggregateId, ex.getMessage(), ex);
            return null;
        }
    }
}
