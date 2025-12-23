package com.project.akka.order;

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
import com.project.domain.order.aggregate.vo.OrderDetail;
import com.project.domain.order.aggregate.vo.OrderStatus;
import com.project.integration.IntegrationMessage;
import com.project.integration.IntegrationOutboxPublisher;
import com.project.integration.message.ProductReleaseRequest;
import com.project.integration.message.ProductReserveRequest;
import com.project.integration.message.PaymentRequested;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class OrderBehavior extends EventSourcedBehaviorWithEnforcedReplies<OrderCommand, OrderEvent, OrderState> {

    private final IntegrationOutboxPublisher outboxPublisher;
    private final UUID orderId;

    public static Behavior<OrderCommand> create(UUID orderId,
                                                IntegrationOutboxPublisher outboxPublisher) {
        return Behaviors.setup(ctx -> new OrderBehavior(orderId, outboxPublisher));
    }

    private OrderBehavior(UUID orderId,
                          IntegrationOutboxPublisher outboxPublisher) {
        super(PersistenceId.ofUniqueId("Order|" + orderId));
        this.orderId = orderId;
        this.outboxPublisher = outboxPublisher;
    }

    @Override
    public OrderState emptyState() {
        return OrderState.empty(orderId);
    }

    @Override
    public CommandHandlerWithReply<OrderCommand, OrderEvent, OrderState> commandHandler() {
        CommandHandlerWithReplyBuilder<OrderCommand, OrderEvent, OrderState> builder =
                newCommandHandlerWithReplyBuilder();

        builder.forAnyState()
                .onCommand(OrderCommand.CreateOrder.class, this::handleCreate)
                .onCommand(OrderCommand.ConfirmStock.class, this::handleConfirm)
                .onCommand(OrderCommand.OutOfStock.class, this::handleOutOfStock)
                .onCommand(OrderCommand.CancelOrder.class, this::handleCancel)
                .onCommand(OrderCommand.MarkPaid.class, this::handleMarkPaid)
                .onCommand(OrderCommand.GetState.class,
                        (state, cmd) -> Effect().reply(cmd.replyTo(), state));

        return builder.build();
    }

    @Override
    public EventHandler<OrderState, OrderEvent> eventHandler() {
        EventHandlerBuilder<OrderState, OrderEvent> builder = newEventHandlerBuilder();
        builder.forAnyState().onAnyEvent((state, event) -> onEvent(state, event));
        return builder.build();
    }

    @Override
    public RetentionCriteria retentionCriteria() {
        return RetentionCriteria.snapshotEvery(100, 2);
    }

    private ReplyEffect<OrderEvent, OrderState> handleCreate(OrderState state,
                                                             OrderCommand.CreateOrder cmd) {
        if (state.userId() != null) {
            return Effect().reply(cmd.replyTo(), StatusReply.error("Order already created"));
        }
        if (cmd.orderDetails() == null || cmd.orderDetails().isEmpty()) {
            return Effect().reply(cmd.replyTo(), StatusReply.error("Order details cannot be empty"));
        }
        var event = new OrderEvent.OrderCreated(cmd.userId(), new ArrayList<>(cmd.orderDetails()), OffsetDateTime.now());
        return Effect().persist(event)
                .thenReply(cmd.replyTo(), newState -> {
                    emitReserveRequests(newState);
                    return StatusReply.success(newState);
                });
    }

    private ReplyEffect<OrderEvent, OrderState> handleConfirm(OrderState state,
                                                              OrderCommand.ConfirmStock cmd) {
        if (!(state.status() == OrderStatus.CREATED || state.status() == OrderStatus.OUT_OF_STOCK)) {
            return Effect().reply(cmd.replyTo(), StatusReply.error("Cannot confirm stock in state " + state.status()));
        }
        var event = new OrderEvent.InventoryConfirmed(cmd.inventoryId(), OffsetDateTime.now());
        return Effect().persist(event)
                .thenReply(cmd.replyTo(), newState -> {
                    emitPaymentRequest(newState);
                    return StatusReply.success(newState);
                });
    }

    private ReplyEffect<OrderEvent, OrderState> handleOutOfStock(OrderState state,
                                                                 OrderCommand.OutOfStock cmd) {
        if (state.status() != OrderStatus.CREATED) {
            return Effect().reply(cmd.replyTo(), StatusReply.error("Cannot mark out of stock in state " + state.status()));
        }
        var event = new OrderEvent.InventoryOutOfStock(cmd.reason(), OffsetDateTime.now());
        return Effect().persist(event)
                .thenReply(cmd.replyTo(), newState -> {
                    emitReleaseRequests(newState);
                    return StatusReply.success(newState);
                });
    }

    private ReplyEffect<OrderEvent, OrderState> handleCancel(OrderState state,
                                                             OrderCommand.CancelOrder cmd) {
        if (state.status() == OrderStatus.PAID || state.status() == OrderStatus.CANCELLED) {
            return Effect().reply(cmd.replyTo(), StatusReply.error("Cannot cancel order in state " + state.status()));
        }
        if (state.userId() != null && !state.userId().equals(cmd.userId())) {
            return Effect().reply(cmd.replyTo(), StatusReply.error("User not allowed to cancel this order"));
        }

        var event = new OrderEvent.OrderCancelled(cmd.userId(), cmd.reason(), OffsetDateTime.now());
        return Effect().persist(event)
                .thenReply(cmd.replyTo(), newState -> {
                    emitReleaseRequests(newState);
                    return StatusReply.success(newState);
                });
    }

    private ReplyEffect<OrderEvent, OrderState> handleMarkPaid(OrderState state,
                                                               OrderCommand.MarkPaid cmd) {
        if (state.status() != OrderStatus.CONFIRMED) {
            return Effect().reply(cmd.replyTo(), StatusReply.error("Cannot mark paid in state " + state.status()));
        }
        if (state.userId() != null && !state.userId().equals(cmd.userId())) {
            return Effect().reply(cmd.replyTo(), StatusReply.error("Invalid user"));
        }
        var event = new OrderEvent.PaymentReceived(cmd.userId(), OffsetDateTime.now());
        return Effect().persist(event)
                .thenReply(cmd.replyTo(), StatusReply::success);
    }

    private OrderState onEvent(OrderState state, OrderEvent event) {
        return switch (event) {
            case OrderEvent.OrderCreated e -> state.toBuilder()
                    .userId(e.userId())
                    .orderDetails(new ArrayList<>(e.orderDetails()))
                    .status(OrderStatus.CREATED)
                    .createdAt(e.createdAt())
                    .build();
            case OrderEvent.InventoryConfirmed e -> state.toBuilder()
                    .inventoryId(e.inventoryId())
                    .status(OrderStatus.CONFIRMED)
                    .build();
            case OrderEvent.InventoryOutOfStock e -> state.toBuilder()
                    .status(OrderStatus.OUT_OF_STOCK)
                    .build();
            case OrderEvent.OrderCancelled e -> state.toBuilder()
                    .status(OrderStatus.CANCELLED)
                    .build();
            case OrderEvent.PaymentReceived e -> state.toBuilder()
                    .paidAt(e.paidAt())
                    .status(OrderStatus.PAID)
                    .build();
        };
    }

    private void emitReserveRequests(OrderState state) {
        if (state.orderDetails() == null) {
            return;
        }
        String correlationId = state.orderId().toString();
        for (OrderDetail detail : state.orderDetails()) {
            ProductReserveRequest message = new ProductReserveRequest(
                    state.orderId(),
                    detail.getProductId(),
                    detail.getQuantity(),
                    correlationId
            );
            outboxPublisher.save(state.orderId(), IntegrationMessage.builder()
                    .type(ProductReserveRequest.class.getSimpleName())
                    .payload(Map.of(
                            "orderId", message.orderId(),
                            "productId", message.productId(),
                            "quantity", message.quantity(),
                            "correlationId", message.correlationId()
                    ))
                    .build());
        }
    }

    private void emitReleaseRequests(OrderState state) {
        if (state.orderDetails() == null) {
            return;
        }
        String correlationId = state.orderId().toString();
        for (OrderDetail detail : state.orderDetails()) {
            ProductReleaseRequest message = new ProductReleaseRequest(
                    state.orderId(),
                    detail.getProductId(),
                    correlationId
            );
            outboxPublisher.save(state.orderId(), IntegrationMessage.builder()
                    .type(ProductReleaseRequest.class.getSimpleName())
                    .payload(Map.of(
                            "orderId", message.orderId(),
                            "productId", message.productId(),
                            "correlationId", message.correlationId()
                    ))
                    .build());
        }
    }

    private void emitPaymentRequest(OrderState state) {
        if (state.orderDetails() == null || state.orderDetails().isEmpty()) {
            return;
        }
        var total = state.orderDetails().stream()
                .map(d -> d.getPrice().multiply(java.math.BigDecimal.valueOf(d.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        String correlationId = state.orderId().toString();
        var message = new PaymentRequested(state.orderId(), state.userId(), total, correlationId);
        outboxPublisher.save(state.orderId(), IntegrationMessage.builder()
                .type(PaymentRequested.class.getSimpleName())
                .payload(Map.of(
                        "orderId", message.orderId(),
                        "userId", message.userId(),
                        "amount", message.amount(),
                        "correlationId", message.correlationId()
                ))
                .build());
    }
}
