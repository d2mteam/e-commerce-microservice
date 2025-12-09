package com.project.domain.order.command;

import com.project.domain.order.aggregate.OrderAggregate;
import com.project.domain.order.aggregate.vo.OrderDetail;
import com.project.event_sourcing_core.domain.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class CreateOrderCommand extends Command {
    private final List<OrderDetail> orderDetails;
    private final UUID userId;

    @Builder
    public CreateOrderCommand(UUID aggregateId, List<OrderDetail> orderDetails, UUID userId) {
        super(OrderAggregate.class.getSimpleName(), aggregateId);
        this.orderDetails = orderDetails;
        this.userId = userId;
    }
}

