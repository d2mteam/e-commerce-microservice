package com.project.akka.inventory;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.CommandHandlerWithReplyBuilder;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventHandlerBuilder;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;
import akka.persistence.typed.javadsl.ReplyEffect;
import akka.persistence.typed.javadsl.RetentionCriteria;
import akka.pattern.StatusReply;
import com.project.integration.IntegrationMessage;
import com.project.integration.IntegrationOutboxPublisher;
import com.project.integration.message.ProductReleaseReply;
import com.project.integration.message.ProductReserveReply;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class InventoryBehavior extends EventSourcedBehaviorWithEnforcedReplies<InventoryCommand, InventoryEvent, InventoryState> {

    private final IntegrationOutboxPublisher outboxPublisher;
    private final UUID inventoryId;

    public static Behavior<InventoryCommand> create(UUID inventoryId,
                                                    IntegrationOutboxPublisher outboxPublisher) {
        return Behaviors.setup(ctx -> new InventoryBehavior(inventoryId, outboxPublisher));
    }

    private InventoryBehavior(UUID inventoryId,
                              IntegrationOutboxPublisher outboxPublisher) {
        super(PersistenceId.ofUniqueId("Inventory|" + inventoryId));
        this.inventoryId = inventoryId;
        this.outboxPublisher = outboxPublisher;
    }

    @Override
    public InventoryState emptyState() {
        return InventoryState.empty(inventoryId);
    }

    @Override
    public CommandHandlerWithReply<InventoryCommand, InventoryEvent, InventoryState> commandHandler() {
        CommandHandlerWithReplyBuilder<InventoryCommand, InventoryEvent, InventoryState> builder =
                newCommandHandlerWithReplyBuilder();

        builder.forAnyState()
                .onCommand(InventoryCommand.CreateInventory.class, this::handleCreate)
                .onCommand(InventoryCommand.AddStock.class, this::handleAddStock)
                .onCommand(InventoryCommand.ReserveStock.class, this::handleReserve)
                .onCommand(InventoryCommand.CancelReservation.class, this::handleCancelReservation)
                .onCommand(InventoryCommand.ReleaseStock.class, this::handleReleaseStock)
                .onCommand(InventoryCommand.GetState.class,
                        (state, cmd) -> Effect().reply(cmd.replyTo(), state));

        return builder.build();
    }

    @Override
    public EventHandler<InventoryState, InventoryEvent> eventHandler() {
        EventHandlerBuilder<InventoryState, InventoryEvent> builder = newEventHandlerBuilder();
        builder.forAnyState().onAnyEvent(this::onEvent);
        return builder.build();
    }

    @Override
    public RetentionCriteria retentionCriteria() {
        return RetentionCriteria.snapshotEvery(100, 2);
    }

    private ReplyEffect<InventoryEvent, InventoryState> handleCreate(InventoryState state,
                                                                     InventoryCommand.CreateInventory cmd) {
        if (state.sku() != null) {
            return Effect().reply(cmd.replyTo(), StatusReply.error("Inventory already created"));
        }
        var event = new InventoryEvent.InventoryCreated(cmd.sku(), cmd.initialQuantity(), OffsetDateTime.now());
        return Effect().persist(event)
                .thenReply(cmd.replyTo(), StatusReply::success);
    }

    private ReplyEffect<InventoryEvent, InventoryState> handleAddStock(InventoryState state,
                                                                       InventoryCommand.AddStock cmd) {
        if (!state.isActive() || state.sku() == null) {
            return Effect().reply(cmd.replyTo(), StatusReply.error("Inventory not active or not created"));
        }
        if (cmd.quantity() <= 0) {
            return Effect().reply(cmd.replyTo(), StatusReply.error("Quantity must be positive"));
        }

        var event = new InventoryEvent.StockAdded(cmd.quantity(), OffsetDateTime.now());
        return Effect().persist(event)
                .thenReply(cmd.replyTo(), StatusReply::success);
    }

    private ReplyEffect<InventoryEvent, InventoryState> handleReserve(InventoryState state,
                                                                      InventoryCommand.ReserveStock cmd) {
        if (!state.isActive()) {
            emitReserveFailure(cmd, "Inventory not active");
            return Effect().reply(cmd.replyTo(), StatusReply.error("Inventory not active"));
        }
        if (cmd.quantity() <= 0) {
            emitReserveFailure(cmd, "Quantity must be positive");
            return Effect().reply(cmd.replyTo(), StatusReply.error("Quantity must be positive"));
        }
        if (cmd.quantity() > state.availableQuantity()) {
            emitReserveFailure(cmd, "Not enough stock");
            return Effect().reply(cmd.replyTo(), StatusReply.error("Not enough stock to reserve"));
        }
        if (state.reservations() != null && state.reservations().containsKey(cmd.orderId())) {
            emitReserveFailure(cmd, "Order already reserved");
            return Effect().reply(cmd.replyTo(), StatusReply.error("Order already reserved"));
        }

        var event = new InventoryEvent.StockReserved(cmd.orderId(), cmd.quantity(), OffsetDateTime.now());
        return Effect().persist(event)
                .thenReply(cmd.replyTo(), newState -> {
                    emitReserveSuccess(cmd);
                    return StatusReply.success(newState);
                });
    }

    private ReplyEffect<InventoryEvent, InventoryState> handleCancelReservation(InventoryState state,
                                                                                InventoryCommand.CancelReservation cmd) {
        if (!state.isActive()) {
            return Effect().reply(cmd.replyTo(), StatusReply.error("Inventory not active"));
        }
        if (state.reservations() == null || !state.reservations().containsKey(cmd.orderId())) {
            return Effect().reply(cmd.replyTo(), StatusReply.error("Order has no active reservation"));
        }
        var event = new InventoryEvent.ReservationCancelled(cmd.orderId(), OffsetDateTime.now());
        return Effect().persist(event)
                .thenReply(cmd.replyTo(), StatusReply::success);
    }

    private ReplyEffect<InventoryEvent, InventoryState> handleReleaseStock(InventoryState state,
                                                                           InventoryCommand.ReleaseStock cmd) {
        if (!state.isActive()) {
            emitReleaseFailure(cmd, "Inventory not active");
            return Effect().reply(cmd.replyTo(), StatusReply.error("Inventory not active"));
        }
        if (state.reservations() == null || !state.reservations().containsKey(cmd.orderId())) {
            emitReleaseFailure(cmd, "Order has no reservation");
            return Effect().reply(cmd.replyTo(), StatusReply.error("Order has no reservation"));
        }

        var event = new InventoryEvent.StockReleased(cmd.orderId(), OffsetDateTime.now());
        return Effect().persist(event)
                .thenReply(cmd.replyTo(), newState -> {
                    emitReleaseSuccess(cmd);
                    return StatusReply.success(newState);
                });
    }

    private InventoryState onEvent(InventoryState state, InventoryEvent event) {
        return switch (event) {
            case InventoryEvent.InventoryCreated e -> state.toBuilder()
                    .sku(e.sku())
                    .availableQuantity(e.initialQuantity())
                    .reservedQuantity(0)
                    .status(InventoryStatus.ACTIVE)
                    .createdAt(e.createdAt())
                    .updatedAt(e.createdAt())
                    .build();
            case InventoryEvent.StockAdded e -> state.toBuilder()
                    .availableQuantity(state.availableQuantity() + e.quantity())
                    .updatedAt(e.addedAt())
                    .build();
            case InventoryEvent.StockReserved e -> state.toBuilder()
                    .availableQuantity(state.availableQuantity() - e.quantity())
                    .reservedQuantity(state.reservedQuantity() + e.quantity())
                    .reservations(mergeReservation(state, e.orderId(), e.quantity()))
                    .updatedAt(e.reservedAt())
                    .build();
            case InventoryEvent.ReservationCancelled e -> {
                int qty = state.reservations() != null ? state.reservations().getOrDefault(e.orderId(), 0) : 0;
                yield state.toBuilder()
                        .availableQuantity(state.availableQuantity() + qty)
                        .reservedQuantity(Math.max(0, state.reservedQuantity() - qty))
                        .reservations(removeReservation(state, e.orderId()))
                        .updatedAt(e.cancelledAt())
                        .build();
            }
            case InventoryEvent.StockReleased e -> {
                int qty = state.reservations() != null ? state.reservations().getOrDefault(e.orderId(), 0) : 0;
                yield state.toBuilder()
                        .reservedQuantity(Math.max(0, state.reservedQuantity() - qty))
                        .reservations(removeReservation(state, e.orderId()))
                        .updatedAt(e.releasedAt())
                        .build();
            }
        };
    }

    private static Map<UUID, Integer> mergeReservation(InventoryState state, UUID orderId, int qty) {
        Map<UUID, Integer> next = new HashMap<>(state.reservations() == null ? Map.of() : state.reservations());
        next.put(orderId, qty);
        return next;
    }

    private static Map<UUID, Integer> removeReservation(InventoryState state, UUID orderId) {
        Map<UUID, Integer> next = new HashMap<>(state.reservations() == null ? Map.of() : state.reservations());
        next.remove(orderId);
        return next;
    }

    private void emitReserveSuccess(InventoryCommand.ReserveStock cmd) {
        var payload = new ProductReserveReply(
                cmd.orderId(),
                cmd.inventoryId(),
                ProductReserveReply.Result.SUCCESS,
                null,
                cmd.correlationId() != null ? cmd.correlationId() : cmd.orderId().toString()
        );
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", payload.orderId());
        data.put("productId", payload.productId());
        data.put("result", payload.result().name());
        if (payload.reason() != null) {
            data.put("reason", payload.reason());
        }
        data.put("correlationId", payload.correlationId());

        outboxPublisher.save(cmd.inventoryId(), IntegrationMessage.builder()
                .type(ProductReserveReply.class.getSimpleName())
                .payload(data)
                .build());
    }

    private void emitReserveFailure(InventoryCommand.ReserveStock cmd,
                                    String reason) {
        var payload = new ProductReserveReply(
                cmd.orderId(),
                cmd.inventoryId(),
                ProductReserveReply.Result.FAILURE,
                reason,
                cmd.correlationId() != null ? cmd.correlationId() : cmd.orderId().toString()
        );
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", payload.orderId());
        data.put("productId", payload.productId());
        data.put("result", payload.result().name());
        data.put("reason", payload.reason());
        data.put("correlationId", payload.correlationId());

        outboxPublisher.save(cmd.inventoryId(), IntegrationMessage.builder()
                .type(ProductReserveReply.class.getSimpleName())
                .payload(data)
                .build());
    }

    private void emitReleaseSuccess(InventoryCommand.ReleaseStock cmd) {
        var payload = new ProductReleaseReply(
                cmd.orderId(),
                cmd.inventoryId(),
                ProductReleaseReply.Result.SUCCESS,
                null,
                cmd.orderId().toString()
        );
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", payload.orderId());
        data.put("productId", payload.productId());
        data.put("result", payload.result().name());
        if (payload.reason() != null) {
            data.put("reason", payload.reason());
        }
        data.put("correlationId", payload.correlationId());

        outboxPublisher.save(cmd.inventoryId(), IntegrationMessage.builder()
                .type(ProductReleaseReply.class.getSimpleName())
                .payload(data)
                .build());
    }

    private void emitReleaseFailure(InventoryCommand.ReleaseStock cmd,
                                    String reason) {
        var payload = new ProductReleaseReply(
                cmd.orderId(),
                cmd.inventoryId(),
                ProductReleaseReply.Result.FAILURE,
                reason,
                cmd.orderId().toString()
        );
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", payload.orderId());
        data.put("productId", payload.productId());
        data.put("result", payload.result().name());
        data.put("reason", payload.reason());
        data.put("correlationId", payload.correlationId());

        outboxPublisher.save(cmd.inventoryId(), IntegrationMessage.builder()
                .type(ProductReleaseReply.class.getSimpleName())
                .payload(data)
                .build());
    }
}