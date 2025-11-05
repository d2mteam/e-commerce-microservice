package com.project.orderservice.domain.command;

import com.project.orderservice.domain.OrderAggregate;
import com.project.orderservice.domain.OrderDetail;
import com.project.orderservice.event_sourcing_core.domain.command.Command;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

@Getter
public class CreateOrderCommand extends Command {
    @NonNull
    private final List<OrderDetail> orderDetails;

    private final UUID userId;

    @Builder
    public CreateOrderCommand(UUID aggregateId, @NonNull List<OrderDetail> orderDetails, UUID userId) {
        super(OrderAggregate.class.getSimpleName(), aggregateId);
        this.orderDetails = orderDetails;
        this.userId = userId;
    }
}

