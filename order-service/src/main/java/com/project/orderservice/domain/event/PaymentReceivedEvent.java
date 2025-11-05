package com.project.orderservice.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.orderservice.event_sourcing_core.domain.event.Event;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class PaymentReceivedEvent extends Event {
    private final UUID userId;
    private final BigDecimal amount;

    @JsonCreator
    @Builder
    public PaymentReceivedEvent(
            @JsonProperty("aggregateId") UUID aggregateId,
            @JsonProperty("version") int version,
            @JsonProperty("userId") UUID userId,
            @JsonProperty("amount") BigDecimal amount
    ) {
        super(aggregateId, version);
        this.userId = userId;
        this.amount = amount;
    }
}
