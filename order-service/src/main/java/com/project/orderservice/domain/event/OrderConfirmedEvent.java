package com.project.orderservice.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.project.orderservice.event_sourcing_core.domain.event.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

@Getter
public class OrderConfirmedEvent extends Event {

    @JsonCreator
    @Builder
    public OrderConfirmedEvent(UUID aggregateId, int version) {
        super(aggregateId, version);
    }

    @Override
    public @NonNull String getEventType() {
        return this.getClass().getSimpleName();
    }
}