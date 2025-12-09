package com.project.domain.order.command;

import com.project.domain.order.aggregate.OrderAggregate;
import com.project.event_sourcing_core.domain.command.Command;
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
