package com.project.domain.inventory.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.event_sourcing_core.domain.event.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

@Getter
public class StockReservedEvent extends Event {
    private UUID orderId;
    private final int quantity;

    @JsonCreator
    @Builder
    public StockReservedEvent(
            @JsonProperty("aggregateId") UUID aggregateId,
            @JsonProperty("version") int version,
            @JsonProperty("orderId") UUID orderId,
            @JsonProperty("quantity") int quantity
    ) {
        super(aggregateId, version);
        this.quantity = quantity;
        this.orderId = orderId;
    }

    @Override
    public @NonNull String getEventType() {
        return this.getClass().getSimpleName();
    }
}
