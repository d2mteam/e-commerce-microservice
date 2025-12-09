package com.project.event_sourcing_core.error;

public class OptimisticConcurrencyControlException extends AggregateStateException {

    public OptimisticConcurrencyControlException(long expectedVersion) {
        super("Actual version doesn't match expected version %s", expectedVersion);
    }
}
