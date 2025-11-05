package com.project.orderservice.event_sourcing.domain.event;

import java.math.BigInteger;

public record EventWithId<T extends Event>(
        long id,
        BigInteger transactionId,
        T event
) {
}
