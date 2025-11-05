package com.project.orderservice.domain.command;

import com.project.orderservice.domain.OrderAggregate;
import com.project.orderservice.event_sourcing_core.domain.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ConfirmOrderCommand extends Command {
    @Builder
    public ConfirmOrderCommand(UUID aggregateId) {
        super(OrderAggregate.class.getSimpleName(), aggregateId);
    }
}

