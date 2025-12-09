package com.project.domain.inventory.command;

import com.project.domain.inventory.aggregate.InventoryAggregate;
import com.project.event_sourcing_core.domain.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AddStockCommand extends Command {
    private final int quantity;

    @Builder
    public AddStockCommand(UUID aggregateId, int quantity) {
        super(InventoryAggregate.class.getSimpleName(), aggregateId);
        this.quantity = quantity;
    }
}