package com.project.orderservice.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.orderservice.domain.OrderDetail;
import com.project.orderservice.event_sourcing_core.domain.event.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

@Getter
public class OrderCreatedEvent extends Event {
    private final UUID userId;
    private final List<OrderDetail> orderDetails;

    @JsonCreator
    @Builder
    public OrderCreatedEvent(
            @JsonProperty("aggregateId") UUID aggregateId,
            @JsonProperty("version") int version,
            @JsonProperty("userId") UUID userId,
            @JsonProperty("orderDetails") List<OrderDetail> orderDetails
    ) {
        super(aggregateId, version);
        this.userId = userId;
        this.orderDetails = orderDetails;
    }

    @Override
    public @NonNull String getEventType() {
        return this.getClass().getSimpleName();
    }
}
