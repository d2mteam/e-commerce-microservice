package com.project.domain.order.command;

import com.project.domain.order.aggregate.OrderAggregate;
import com.project.event_sourcing_core.domain.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class OutOfStockCommand extends Command {
    private final String reason;

    @Builder
    public OutOfStockCommand(UUID aggregateId, String reason) {
        super(OrderAggregate.class.getSimpleName(), aggregateId);
        this.reason = reason;
    }
}
