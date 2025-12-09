package com.project.application.event;

import com.project.application.integration.IntegrationEvent;
import com.project.application.integration.mapper.DomainToIntegrationEventMapper;
import com.project.application.service.IntegrationEventOutboxWriter;
import com.project.domain.inventory.aggregate.InventoryAggregate;
import com.project.event_sourcing_core.domain.Aggregate;
import com.project.event_sourcing_core.domain.event.Event;
import com.project.event_sourcing_core.domain.event.EventWithId;
import com.project.event_sourcing_core.service.event.SyncEventHandler;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class InventorySyncEventHandler implements SyncEventHandler {

    private final List<DomainToIntegrationEventMapper<?>> mappers;
    private final IntegrationEventOutboxWriter outboxWriter;

    @Override
    public void handleEvents(List<EventWithId<Event>> events, Aggregate aggregate) {
        for (EventWithId<Event> eventWithId : events) {
            Event domainEvent = eventWithId.event();

            mappers.stream()
                    .filter(mapper -> mapper.supports(domainEvent.getClass()))
                    .findFirst()
                    .ifPresent(mapper -> {
                        @SuppressWarnings("unchecked")
                        DomainToIntegrationEventMapper<Event> casted = (DomainToIntegrationEventMapper<Event>) mapper;
                        List<IntegrationEvent> integrationEvents = casted.map(domainEvent);

                        integrationEvents.forEach(integrationEvent -> {
                            try {
                                outboxWriter.persist(domainEvent.getAggregateId(), integrationEvent);
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
        return InventoryAggregate.class.getSimpleName();
    }
}
