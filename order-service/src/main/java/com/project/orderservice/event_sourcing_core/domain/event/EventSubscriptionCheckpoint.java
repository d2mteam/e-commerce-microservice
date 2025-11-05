package com.project.orderservice.event_sourcing.domain.event;

import java.math.BigInteger;

public record EventSubscriptionCheckpoint(
        BigInteger lastProcessedTransactionId,
        long lastProcessedEventId
) {
}
