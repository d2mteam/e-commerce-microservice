package com.project.orderservice.event_sourcing_core.service.event;


import com.project.orderservice.event_sourcing_core.domain.Aggregate;
import com.project.orderservice.event_sourcing_core.domain.event.Event;
import com.project.orderservice.event_sourcing_core.domain.event.EventWithId;
import jakarta.annotation.Nonnull;

import java.util.List;

public interface SyncEventHandler {

    void handleEvents(List<EventWithId<Event>> events, Aggregate aggregate);

    @Nonnull
    String getAggregateType();
}
