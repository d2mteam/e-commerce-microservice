package com.project.akka.order;

import com.project.akka.serialization.CborSerializable;
import com.project.domain.order.aggregate.vo.OrderDetail;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public sealed interface OrderEvent extends CborSerializable
        permits OrderEvent.OrderCreated,
                OrderEvent.InventoryConfirmed,
                OrderEvent.InventoryOutOfStock,
                OrderEvent.OrderCancelled,
                OrderEvent.PaymentReceived {

    record OrderCreated(UUID userId,
                        List<OrderDetail> orderDetails,
                        OffsetDateTime createdAt) implements OrderEvent {
    }

    record InventoryConfirmed(UUID inventoryId,
                              OffsetDateTime confirmedAt) implements OrderEvent {
    }

    record InventoryOutOfStock(String reason,
                               OffsetDateTime occurredAt) implements OrderEvent {
    }

    record OrderCancelled(UUID userId,
                          String reason,
                          OffsetDateTime cancelledAt) implements OrderEvent {
    }

    record PaymentReceived(UUID userId,
                           OffsetDateTime paidAt) implements OrderEvent {
    }
}
