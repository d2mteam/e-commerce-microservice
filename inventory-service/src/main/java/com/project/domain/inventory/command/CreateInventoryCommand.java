package com.project.domain.inventory.command;

import com.project.domain.inventory.aggregate.InventoryAggregate;
import com.project.event_sourcing_core.domain.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CreateInventoryCommand extends Command {
    private final String sku;
    private final int initialQuantity;

    @Builder
    public CreateInventoryCommand(UUID aggregateId, String sku, int initialQuantity) {
        super(InventoryAggregate.class.getSimpleName(), aggregateId);
        this.sku = sku;
        this.initialQuantity = initialQuantity;
    }
}