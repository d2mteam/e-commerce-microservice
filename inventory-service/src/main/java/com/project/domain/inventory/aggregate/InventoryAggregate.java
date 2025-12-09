package com.project.domain.inventory.aggregate;

import com.project.domain.inventory.command.*;
import com.project.domain.inventory.event.*;
import com.project.event_sourcing_core.domain.Aggregate;
import com.project.event_sourcing_core.error.AggregateStateException;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.NonNull;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class InventoryAggregate extends Aggregate {
    private String sku;
    private int availableQuantity;
    private int reservedQuantity;
    private InventoryStatus status = InventoryStatus.CREATED;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // orderId → quantity reserved
    private final Map<UUID, Integer> reservations = new HashMap<>();

    public InventoryAggregate(@NonNull UUID aggregateId, int version) {
        super(aggregateId, version);
    }

    @Nonnull
    @Override
    public String getAggregateType() {
        return this.getClass().getSimpleName();
    }

    // =================== COMMAND HANDLERS ===================

    public void process(CreateInventoryCommand cmd) {
        if (status != InventoryStatus.CREATED || this.sku != null) {
            throw new AggregateStateException("Inventory already created or invalid state");
        }
        applyChange(InventoryCreatedEvent.builder()
                .aggregateId(getAggregateId())
                .version(getNextVersion())
                .sku(cmd.getSku())
                .quantity(cmd.getInitialQuantity())
                .createdAt(OffsetDateTime.now())
                .build());
    }

    public void process(AddStockCommand cmd) {
        ensureActive();
        applyChange(StockAddedEvent.builder()
                .aggregateId(getAggregateId())
                .version(getNextVersion())
                .quantity(cmd.getQuantity())
                .build());
    }

    public void process(ReserveStockCommand cmd) {
        ensureActive();

        if (cmd.getQuantity() > availableQuantity) {
            throw new AggregateStateException("Not enough stock to reserve");
        }

        if (reservations.containsKey(cmd.getOrderId())) {
            throw new AggregateStateException("Order already reserved stock");
        }

        applyChange(StockReservedEvent.builder()
                .aggregateId(getAggregateId())
                .version(getNextVersion())
                .orderId(cmd.getOrderId())
                .quantity(cmd.getQuantity())
                .build());
    }

    public void process(CancelReservationCommand cmd) {
        ensureActive();

        Integer reservedQty = reservations.get(cmd.getOrderId());
        if (reservedQty == null || reservedQty <= 0) {
            throw new AggregateStateException("Order has no active reservation");
        }

        applyChange(ReservationCancelledEvent.builder()
                .aggregateId(getAggregateId())
                .version(getNextVersion())
                .orderId(cmd.getOrderId())
                .build());
    }

    public void process(ReleaseStockCommand cmd) {
        ensureActive();

        Integer reservedQty = reservations.get(cmd.getOrderId());
        if (reservedQty == null || reservedQty <= 0) {
            throw new AggregateStateException("Order has no reserved stock");
        }

        applyChange(StockReleasedEvent.builder()
                .aggregateId(getAggregateId())
                .version(getNextVersion())
                .orderId(cmd.getOrderId())
                .build());
    }

    public void apply(InventoryCreatedEvent e) {
        this.sku = e.getSku();
        this.availableQuantity = e.getQuantity();
        this.reservedQuantity = 0;
        this.status = InventoryStatus.ACTIVE;
        this.createdAt = e.getCreatedAt();
        this.updatedAt = this.createdAt;
    }

    public void apply(StockAddedEvent e) {
        this.availableQuantity += e.getQuantity();
        this.updatedAt = OffsetDateTime.now();
    }

    public void apply(StockReservedEvent e) {
        this.availableQuantity -= e.getQuantity();
        this.reservedQuantity += e.getQuantity();
        reservations.put(e.getOrderId(), e.getQuantity());
        this.updatedAt = OffsetDateTime.now();
    }

    public void apply(ReservationCancelledEvent e) {
        Integer qty = reservations.get(e.getOrderId());
        if (qty == null) qty = 0;

        this.availableQuantity += qty;
        this.reservedQuantity -= qty;
        reservations.remove(e.getOrderId());

        this.updatedAt = OffsetDateTime.now();
    }

    public void apply(StockReleasedEvent e) {
        Integer qty = reservations.get(e.getOrderId());
        if (qty == null) qty = 0;

        this.reservedQuantity -= qty;
        reservations.remove(e.getOrderId());

        this.updatedAt = OffsetDateTime.now();
    }

    private void ensureActive() {
        if (!EnumSet.of(InventoryStatus.CREATED, InventoryStatus.ACTIVE).contains(status)) {
            throw new AggregateStateException("Inventory not active");
        }
    }
}
