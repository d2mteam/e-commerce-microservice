package com.project.orderservice.domain.event;

import com.project.orderservice.event_sourcing.domain.event.Event;
import lombok.NonNull;

import java.util.UUID;

public class OrderCancelledEvent extends Event {
    public OrderCancelledEvent(UUID aggregateId, int version) {
        super(aggregateId, version);
    }

    @Override
    public @NonNull String getEventType() {
        return "";
    }
}