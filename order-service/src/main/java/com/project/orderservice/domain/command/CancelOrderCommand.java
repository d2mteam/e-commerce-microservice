package com.project.orderservice.domain.command;

import com.project.orderservice.domain.OrderAggregate;
import com.project.orderservice.event_sourcing_core.domain.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CancelOrderCommand extends Command {
    private final UUID userId;
    private final String reason;

    @Builder
    public CancelOrderCommand(UUID aggregateId, UUID userId, String reason) {
        super(OrderAggregate.class.getSimpleName(), aggregateId);
        this.userId = userId;
        this.reason = reason;
    }
}
