package com.project.domain.order.command;

import com.project.domain.order.aggregate.OrderAggregate;
import com.project.event_sourcing_core.domain.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ConfirmStockCommand extends Command {
    private final UUID userId;
    private final UUID inventoryId;

    @Builder
    public ConfirmStockCommand(UUID aggregateId, UUID userId, UUID inventoryId) {
        super(OrderAggregate.class.getSimpleName(), aggregateId);
        this.userId = userId;
        this.inventoryId = inventoryId;
    }
}