package com.project.orderservice.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.orderservice.event_sourcing_core.domain.event.Event;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class InventoryOutOfStockEvent extends Event {
    private final UUID userId;
    private final String reason;

    @JsonCreator
    @Builder
    public InventoryOutOfStockEvent(
            @JsonProperty("aggregateId") UUID aggregateId,
            @JsonProperty("version") int version,
            @JsonProperty("userId") UUID userId,
            @JsonProperty("reason") String reason
    ) {
        super(aggregateId, version);
        this.userId = userId;
        this.reason = reason;
    }
}
