package com.project.domain.order.command;

import com.project.domain.order.aggregate.OrderAggregate;
import com.project.event_sourcing_core.domain.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class StartShippingCommand extends Command {
    private final UUID shipmentId;

    @Builder
    public StartShippingCommand(UUID aggregateId, UUID shipmentId) {
        super(OrderAggregate.class.getSimpleName(), aggregateId);
        this.shipmentId = shipmentId;
    }
}
