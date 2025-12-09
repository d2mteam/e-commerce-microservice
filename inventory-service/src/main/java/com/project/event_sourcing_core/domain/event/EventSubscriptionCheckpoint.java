package com.project.event_sourcing_core.domain.event;

import java.math.BigInteger;

public record EventSubscriptionCheckpoint(
        BigInteger lastProcessedTransactionId,
        long lastProcessedEventId
) {
}
