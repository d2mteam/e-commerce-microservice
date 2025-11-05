package com.project.orderservice.event_sourcing.error;

public class OptimisticConcurrencyControlException extends AggregateStateException {

    public OptimisticConcurrencyControlException(long expectedVersion) {
        super("Actual version doesn't match expected version %s", expectedVersion);
    }
}
