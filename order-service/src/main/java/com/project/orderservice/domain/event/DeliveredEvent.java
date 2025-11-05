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
public class DeliveredEvent extends Event {
    private final UUID userId;
    private final OffsetDateTime deliveredAt;

    @JsonCreator
    @Builder
    public DeliveredEvent(
            @JsonProperty("aggregateId") UUID aggregateId,
            @JsonProperty("version") int version,
            @JsonProperty("userId") UUID userId,
            @JsonProperty("deliveredAt") OffsetDateTime deliveredAt
    ) {
        super(aggregateId, version);
        this.userId = userId;
        this.deliveredAt = deliveredAt;
    }

    @Override
    public @NonNull String getEventType() {
        return this.getClass().getSimpleName();
    }
}
