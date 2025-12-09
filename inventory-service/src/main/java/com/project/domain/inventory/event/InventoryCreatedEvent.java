package com.project.domain.inventory.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.event_sourcing_core.domain.event.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class InventoryCreatedEvent extends Event {
    private final String sku;
    private final int quantity;
    private final OffsetDateTime createdAt;

    @JsonCreator
    @Builder
    public InventoryCreatedEvent(
            @JsonProperty("aggregateId") UUID aggregateId,
            @JsonProperty("version") int version,
            @JsonProperty("sku") String sku,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("createdAt") OffsetDateTime createdAt
    ) {
        super(aggregateId, version);
        this.sku = sku;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    @Override
    public @NonNull String getEventType() {
        return this.getClass().getSimpleName();
    }
}