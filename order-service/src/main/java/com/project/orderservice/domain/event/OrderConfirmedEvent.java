package com.project.orderservice.domain.event;

import com.project.orderservice.event_sourcing.domain.event.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

@Getter
@Builder
public class OrderConfirmedEvent extends Event {
    public OrderConfirmedEvent(UUID aggregateId, int version) {
        super(aggregateId, version);
    }

    @Override
    public @NonNull String getEventType() {
        return "";
    }
}