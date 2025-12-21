package com.project.akka.inventory;

import com.project.akka.serialization.CborSerializable;

import java.time.OffsetDateTime;
import java.util.UUID;

public sealed interface InventoryEvent extends CborSerializable
        permits InventoryEvent.InventoryCreated,
                InventoryEvent.StockAdded,
                InventoryEvent.StockReserved,
                InventoryEvent.ReservationCancelled,
                InventoryEvent.StockReleased {

    record InventoryCreated(String sku,
                            int initialQuantity,
                            OffsetDateTime createdAt) implements InventoryEvent {
    }

    record StockAdded(int quantity,
                      OffsetDateTime addedAt) implements InventoryEvent {
    }

    record StockReserved(UUID orderId,
                         int quantity,
                         OffsetDateTime reservedAt) implements InventoryEvent {
    }

    record ReservationCancelled(UUID orderId,
                                OffsetDateTime cancelledAt) implements InventoryEvent {
    }

    record StockReleased(UUID orderId,
                         OffsetDateTime releasedAt) implements InventoryEvent {
    }
}
