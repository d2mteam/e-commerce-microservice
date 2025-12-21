package com.project.akka.inventory;

import com.project.akka.serialization.CborSerializable;
import lombok.Builder;
import lombok.Singular;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Builder(toBuilder = true)
public record InventoryState(
        UUID inventoryId,
        String sku,
        int availableQuantity,
        int reservedQuantity,
        InventoryStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        @Singular("reservation") Map<UUID, Integer> reservations
) implements CborSerializable {

    public static InventoryState empty(UUID inventoryId) {
        return InventoryState.builder()
                .inventoryId(inventoryId)
                .availableQuantity(0)
                .reservedQuantity(0)
                .status(InventoryStatus.CREATED)
                .reservations(new HashMap<>())
                .build();
    }

    public boolean isActive() {
        return status == InventoryStatus.CREATED || status == InventoryStatus.ACTIVE;
    }

    public InventoryState withReservation(UUID orderId, int qty) {
        Map<UUID, Integer> next = new HashMap<>(reservations == null ? Map.of() : reservations);
        next.put(orderId, qty);
        return toBuilder()
                .reservations(next)
                .build();
    }

    public InventoryState withoutReservation(UUID orderId) {
        Map<UUID, Integer> next = new HashMap<>(reservations == null ? Map.of() : reservations);
        next.remove(orderId);
        return toBuilder()
                .reservations(next)
                .build();
    }
}
