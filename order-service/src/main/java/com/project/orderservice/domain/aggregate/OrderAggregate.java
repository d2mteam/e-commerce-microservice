package com.project.orderservice.domain.aggregate;

import com.project.orderservice.domain.Aggregate;
import com.project.orderservice.domain.Command;
import com.project.orderservice.domain.Event;
import com.project.orderservice.domain.command.CancelOrderCommand;
import com.project.orderservice.domain.command.CompleteOrderCommand;
import com.project.orderservice.domain.command.CreateOrderCommand;
import com.project.orderservice.domain.event.OrderCancelledEvent;
import com.project.orderservice.domain.event.OrderCompletedEvent;
import com.project.orderservice.domain.event.OrderCreatedEvent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class OrderAggregate extends Aggregate {
    private final List<OrderDetail> orderDetails = new ArrayList<>();
    private OrderStatus status;

    public OrderAggregate(UUID aggregateId) {
        super(aggregateId);
    }

    @Override
    public void process(Command command) {
        switch (command) {
            case CreateOrderCommand c -> {
                if (status != null)
                    throw new IllegalStateException("Order already exists");

                var event = new OrderCreatedEvent(getAggregateId(), getNextVersion(), c.getOrderDetails());
                apply(event);
            }
            case CancelOrderCommand c -> {
                if (status != OrderStatus.CREATED && status != OrderStatus.PROCESSING)
                    throw new IllegalStateException("Order cannot be cancelled in state " + status);

                var event = new OrderCancelledEvent(getAggregateId(), getNextVersion());
                apply(event);

            }
            case CompleteOrderCommand c -> {
                if (status != OrderStatus.PROCESSING)
                    throw new IllegalStateException("Order cannot be completed in state " + status);

                var event = new OrderCompletedEvent(getAggregateId(), getNextVersion());
                apply(event);
            }
            default -> throw new UnsupportedOperationException("Unknown command: " + command.getClass());
        }
    }

    @Override
    public void apply(Event event) {
        switch (event) {
            case OrderCreatedEvent e -> {
                orderDetails.clear();
                orderDetails.addAll(e.getOrderDetails());
                status = OrderStatus.CREATED;
            }
            case OrderCancelledEvent e -> status = OrderStatus.CANCELED;
            case OrderCompletedEvent e -> status = OrderStatus.COMPLETED;
            default -> throw new UnsupportedOperationException("Unknown event: " + event.getClass());
        }
        getChanges().add(event);
    }


    @Override
    public String getAggregateType() {
        return OrderAggregate.class.getSimpleName();
    }
}
