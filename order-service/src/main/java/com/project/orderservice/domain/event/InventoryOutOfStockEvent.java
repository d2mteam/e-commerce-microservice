package com.project.orderservice.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.orderservice.event_sourcing_core.domain.event.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

@Getter
public class InventoryOutOfStockEvent extends Event {
    private final String reason;

    @JsonCreator
    @Builder
    public InventoryOutOfStockEvent(
            @JsonProperty("aggregateId") UUID aggregateId,
            @JsonProperty("version") int version,
            @JsonProperty("reason") String reason
    ) {
        super(aggregateId, version);
        this.reason = reason;
    }

    @Override
    public @NonNull String getEventType() {
        return this.getClass().getSimpleName();
    }
}
