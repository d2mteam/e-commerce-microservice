package com.project.domain.inventory.command;

import com.project.domain.inventory.aggregate.InventoryAggregate;
import com.project.event_sourcing_core.domain.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ReleaseStockCommand extends Command {
    private final UUID orderId;

    @Builder
    public ReleaseStockCommand(UUID aggregateId, UUID orderId) {
        super(InventoryAggregate.class.getSimpleName(), aggregateId);
        this.orderId = orderId;
    }
}
