package com.project.orderservice.event_sourcing_core.service.event;


import com.project.orderservice.event_sourcing_core.domain.event.Event;
import com.project.orderservice.event_sourcing_core.domain.event.EventWithId;
import jakarta.annotation.Nonnull;

public interface AsyncEventHandler {

    void handleEvent(EventWithId<Event> event);

    @Nonnull
    String getAggregateType();

    default String getSubscriptionName() {
        return getClass().getName();
    }
}
