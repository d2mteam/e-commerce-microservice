package com.project.domain.order.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.event_sourcing_core.domain.event.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

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

    @Override
    public @NonNull String getEventType() {
        return this.getClass().getSimpleName();
    }
}
