package com.project.orderservice.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.orderservice.event_sourcing_core.domain.event.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class ShippingStartedEvent extends Event {
    private final UUID shipmentId;
    private final OffsetDateTime startedAt;

    @JsonCreator
    @Builder
    public ShippingStartedEvent(
            @JsonProperty("aggregateId") UUID aggregateId,
            @JsonProperty("version") int version,
            @JsonProperty("shipmentId") UUID shipmentId,
            @JsonProperty("startedAt") OffsetDateTime startedAt
    ) {
        super(aggregateId, version);
        this.shipmentId = shipmentId;
        this.startedAt = startedAt;
    }

    @Override
    public @NonNull String getEventType() {
        return this.getClass().getSimpleName();
    }
}
