package com.project.domain.order.aggregate;

import com.project.domain.order.aggregate.vo.OrderDetail;
import com.project.domain.order.aggregate.vo.OrderStatus;
import com.project.domain.order.command.*;
import com.project.domain.order.event.*;
import com.project.event_sourcing_core.domain.Aggregate;
import com.project.event_sourcing_core.error.AggregateStateException;
import jakarta.annotation.Nonnull;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Getter
public class OrderAggregate extends Aggregate {
    private UUID userId;
    private List<OrderDetail> orderDetails = new ArrayList<>();
    private OrderStatus status = OrderStatus.CREATED;
    private UUID inventoryId;
    private UUID shipmentId;
    private OffsetDateTime deliveredAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime paidAt;

    public OrderAggregate(UUID aggregateId, int version) {
        super(aggregateId, version);
    }

    @Nonnull
    @Override
    public String getAggregateType() {
        return this.getClass().getSimpleName();
    }

    // process event
    public void process(CreateOrderCommand cmd) {
        if (status != OrderStatus.CREATED) {
            throw new AggregateStateException("Order already created or in invalid state");
        }
        applyChange(OrderCreatedEvent.builder()
                .aggregateId(getAggregateId())
                .version(getNextVersion())
                .userId(cmd.getUserId())
                .orderDetails(cmd.getOrderDetails())
                .createdAt(OffsetDateTime.now())
                .build());
    }

    public void process(ConfirmStockCommand cmd) {
        if (status != OrderStatus.CREATED && status != OrderStatus.OUT_OF_STOCK) {
            throw new AggregateStateException("Cannot confirm stock in state %s", status);
        }
        applyChange(InventoryConfirmedEvent.builder()
                .aggregateId(getAggregateId())
                .version(getNextVersion())
                .userId(cmd.getUserId())
                .inventoryId(cmd.getInventoryId())
                .build());
    }

    public void process(OutOfStockCommand cmd) {
        if (status != OrderStatus.CREATED) {
            throw new AggregateStateException("Cannot mark out of stock in state %s", status);
        }
        applyChange(InventoryOutOfStockEvent.builder()
                .aggregateId(getAggregateId())
                .version(getNextVersion())
                .reason(cmd.getReason())
                .build());
    }

    public void process(StartShippingCommand cmd) {
        if (status != OrderStatus.CONFIRMED) {
            throw new AggregateStateException("Cannot start shipping in state %s", status);
        }
        applyChange(ShippingStartedEvent.builder()
                .aggregateId(getAggregateId())
                .version(getNextVersion())
                .shipmentId(cmd.getShipmentId())
                .startedAt(OffsetDateTime.now())
                .build());
    }

    public void process(MarkDeliveredCommand cmd) {
        ensureSameUser(cmd.getUserId());

        if (status != OrderStatus.SHIPPING) {
            throw new AggregateStateException("Cannot mark delivered in state %s", status);
        }
        applyChange(DeliveredEvent.builder()
                .aggregateId(getAggregateId())
                .version(getNextVersion())
                .userId(cmd.getUserId())
                .deliveredAt(cmd.getDeliveredAt())
                .build());
    }

    public void process(MarkPaidCommand cmd) {
        if (status != OrderStatus.DELIVERED) {
            throw new AggregateStateException("Cannot mark paid in state %s", status);
        }
        applyChange(PaymentReceivedEvent.builder()
                .aggregateId(getAggregateId())
                .version(getNextVersion())
                .userId(cmd.getUserId())
                .amount(cmd.getAmount())
                .build());
    }

    public void process(CancelOrderCommand cmd) {
        ensureSameUser(cmd.getUserId());

        if (EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELLED).contains(status)) {
            throw new AggregateStateException("Cannot cancel order in state %s", status);
        }
        applyChange(OrderCancelledEvent.builder()
                .aggregateId(getAggregateId())
                .version(getNextVersion())
                .userId(cmd.getUserId())
                .reason(cmd.getReason())
                .build());
    }

    //apply event
    public void apply(OrderCreatedEvent event) {
        this.userId = event.getUserId();
        this.orderDetails = event.getOrderDetails();
        this.status = OrderStatus.CREATED;
        this.createdAt = event.getCreatedAt();
    }

    public void apply(InventoryConfirmedEvent event) {
        this.inventoryId = event.getInventoryId();
        this.status = OrderStatus.CONFIRMED;
    }

    public void apply(InventoryOutOfStockEvent event) {
        this.status = OrderStatus.OUT_OF_STOCK;
    }

    public void apply(ShippingStartedEvent event) {
        this.shipmentId = event.getShipmentId();
        this.status = OrderStatus.SHIPPING;
    }

    public void apply(DeliveredEvent event) {
        this.deliveredAt = event.getDeliveredAt();
        this.status = OrderStatus.DELIVERED;
    }

    public void apply(PaymentReceivedEvent event) {
        this.paidAt = OffsetDateTime.now();
        this.status = OrderStatus.PAID;
    }

    public void apply(OrderCancelledEvent event) {
        this.status = OrderStatus.CANCELLED;
    }

    private void ensureSameUser(UUID cmdUserId) {
        if (!this.userId.equals(cmdUserId)) {
            throw new AggregateStateException("Invalid user: command userId does not match order owner");
        }
    }
}
