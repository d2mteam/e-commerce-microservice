package com.project.domain.order.command;

import com.project.domain.order.aggregate.OrderAggregate;
import com.project.event_sourcing_core.domain.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class MarkPaidCommand extends Command {
    private final UUID userId;
    private final BigDecimal amount;

    @Builder
    public MarkPaidCommand(UUID aggregateId, UUID userId, BigDecimal amount) {
        super(OrderAggregate.class.getSimpleName(), aggregateId);
        this.userId = userId;
        this.amount = amount;
    }
}
