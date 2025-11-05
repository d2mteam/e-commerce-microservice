package com.project.orderservice.domain.command;

import com.project.orderservice.domain.OrderAggregate;
import com.project.orderservice.event_sourcing_core.domain.command.Command;
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