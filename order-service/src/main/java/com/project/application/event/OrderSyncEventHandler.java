package com.project.application.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.project.application.integration.mapper.DomainToIntegrationEventMapper;
import com.project.application.integration.IntegrationEvent;
import com.project.domain.order.aggregate.OrderAggregate;
import com.project.event_sourcing_core.domain.Aggregate;
import com.project.event_sourcing_core.domain.event.Event;
import com.project.event_sourcing_core.domain.event.EventWithId;
import com.project.event_sourcing_core.service.event.SyncEventHandler;
import com.project.infrastructure.jpa.entity.OutboxEvent;

import com.project.infrastructure.jpa.repository.OutboxEventRepository;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class OrderSyncEventHandler implements SyncEventHandler {

    private final List<DomainToIntegrationEventMapper<?>> mappers;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void handleEvents(List<EventWithId<Event>> events, Aggregate aggregate) {

        for (EventWithId<Event> eventWithId : events) {

            Event domainEvent = eventWithId.event();

            mappers.stream()
                    .filter(mapper -> mapper.supports(domainEvent.getClass()))
                    .findFirst()
                    .ifPresent(mapper -> {

                        @SuppressWarnings("unchecked")
                        var casted = (DomainToIntegrationEventMapper<Event>) mapper;

                        List<IntegrationEvent> integrationEvents =
                                casted.map(domainEvent);

                        integrationEvents.forEach(integrationEvent -> {
                            try {
                                Map<String, Object> payload = objectMapper
                                        .convertValue(integrationEvent, new TypeReference<>() {});

                                OutboxEvent outboxEvent = OutboxEvent.builder()
                                        .aggregateId(domainEvent.getAggregateId())
                                        .eventType(integrationEvent.getEventType())
                                        .payload(payload)
                                        .build();

                                outboxRepository.save(outboxEvent);

                                log.info("Saved outbox event: type={}, aggregateId={}",
                                        integrationEvent.getEventType(), domainEvent.getAggregateId());

                            } catch (Exception ex) {
                                log.error("Failed to save outbox event: {}", ex.getMessage(), ex);
                            }
                        });
                    });
        }
    }

    @Nonnull
    @Override
    public String getAggregateType() {
        return OrderAggregate.class.getSimpleName();
    }
}
