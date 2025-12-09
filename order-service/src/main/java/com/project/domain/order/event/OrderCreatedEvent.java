package com.project.domain.order.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.domain.order.aggregate.vo.OrderDetail;
import com.project.event_sourcing_core.domain.event.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class OrderCreatedEvent extends Event {
    private final UUID userId;
    private final List<OrderDetail> orderDetails;
    private final OffsetDateTime createdAt;

    @JsonCreator
    @Builder
    public OrderCreatedEvent(
            @JsonProperty("aggregateId") UUID aggregateId,
            @JsonProperty("version") int version,
            @JsonProperty("userId") UUID userId,
            @JsonProperty("orderDetails") List<OrderDetail> orderDetails,
            @JsonProperty("createdAt") OffsetDateTime createdAt
    ) {
        super(aggregateId, version);
        this.userId = userId;
        this.orderDetails = orderDetails;
        this.createdAt = createdAt;
    }

    @Override
    public @NonNull String getEventType() {
        return this.getClass().getSimpleName();
    }
}
