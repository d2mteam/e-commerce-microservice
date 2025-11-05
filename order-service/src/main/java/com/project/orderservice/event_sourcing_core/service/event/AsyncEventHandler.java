package com.project.orderservice.event_sourcing.service.event;


import com.project.orderservice.event_sourcing.domain.event.Event;
import com.project.orderservice.event_sourcing.domain.event.EventWithId;
import jakarta.annotation.Nonnull;

public interface AsyncEventHandler {

    void handleEvent(EventWithId<Event> event);

    @Nonnull
    String getAggregateType();

    default String getSubscriptionName() {
        return getClass().getName();
    }
}
