package com.project.domain.inventory.command;

import com.project.domain.inventory.aggregate.InventoryAggregate;
import com.project.event_sourcing_core.domain.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ReserveStockCommand extends Command {
    private final UUID orderId;
    private final int quantity;


    @Builder
    public ReserveStockCommand(UUID aggregateId, UUID orderId, int quantity) {
        super(InventoryAggregate.class.getSimpleName(), aggregateId);
        this.orderId = orderId;
        this.quantity = quantity;
    }
}