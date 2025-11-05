package com.project.orderservice.domain;

import com.project.orderservice.event_sourcing.domain.command.Command;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

@Getter
@Builder
public class CreateOrderCommand extends Command {
    @NonNull
    private final OrderDetail detail;

    public CreateOrderCommand(UUID aggregateId, @NonNull OrderDetail detail) {
        super(OrderAggregate.class.getSimpleName(), aggregateId);
        this.detail = detail;
    }
}

