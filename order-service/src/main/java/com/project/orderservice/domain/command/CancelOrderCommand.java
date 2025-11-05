package com.project.orderservice.domain;

@Getter
@Builder
public class CancelOrderCommand extends Command {
    public CancelOrderCommand(UUID aggregateId) {
        super(aggregateId);
    }
}