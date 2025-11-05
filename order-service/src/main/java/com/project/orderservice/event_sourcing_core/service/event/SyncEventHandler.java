package com.project.orderservice.event_sourcing.service.event;


import com.project.orderservice.event_sourcing.domain.Aggregate;
import com.project.orderservice.event_sourcing.domain.event.Event;
import com.project.orderservice.event_sourcing.domain.event.EventWithId;
import jakarta.annotation.Nonnull;

import java.util.List;

public interface SyncEventHandler {

    void handleEvents(List<EventWithId<Event>> events,
                      Aggregate aggregate);

    @Nonnull
    String getAggregateType();
}
