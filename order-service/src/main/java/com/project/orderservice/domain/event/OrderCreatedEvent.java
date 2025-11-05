package com.project.orderservice.domain.event;

import com.project.orderservice.domain.Event;
import com.project.orderservice.domain.OrderDetail;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class OrderCreatedEvent extends Event {
    private final List<OrderDetail> orderDetails;

    public OrderCreatedEvent(UUID aggregateId, int version, List<OrderDetail> orderDetails) {
        super(aggregateId, version);
        this.orderDetails = orderDetails;
    }

    @Override
    public String getType() {
        return "OrderCreated";
    }
}