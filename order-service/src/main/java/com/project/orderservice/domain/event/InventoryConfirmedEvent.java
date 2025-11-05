package com.project.orderservice.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.orderservice.event_sourcing_core.domain.event.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

@Getter
public class InventoryConfirmedEvent extends Event {
    private final UUID userId;
    private final UUID inventoryId;

    @JsonCreator
    @Builder
    public InventoryConfirmedEvent(
            @JsonProperty("aggregateId") UUID aggregateId,
            @JsonProperty("version") int version,
            @JsonProperty("userId") UUID userId,
            @JsonProperty("inventoryId") UUID inventoryId
    ) {
        super(aggregateId, version);
        this.userId = userId;
        this.inventoryId = inventoryId;
    }

    @Override
    public @NonNull String getEventType() {
        return this.getClass().getSimpleName();
    }
}
