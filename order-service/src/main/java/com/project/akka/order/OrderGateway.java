package com.project.akka.order;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AskPattern;
import akka.pattern.StatusReply;
import com.project.domain.order.aggregate.vo.OrderDetail;
import com.project.integration.IntegrationOutboxPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderGateway {

    private final ActorSystem<Void> actorSystem;
    private final IntegrationOutboxPublisher outboxPublisher;

    private final Map<UUID, ActorRef<OrderCommand>> orderActors = new ConcurrentHashMap<>();
    private final Duration timeout = Duration.ofSeconds(3);

    public CompletionStage<OrderState> createOrder(UUID orderId,
                                                   UUID userId,
                                                   List<OrderDetail> details) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("OrderGateway#create orderId={} userId={} details={}", orderId, userId, details);
        ActorRef<OrderCommand> actor = actorFor(orderId);
        CompletionStage<StatusReply<OrderState>> stage = AskPattern.ask(
                actor,
                replyTo -> new OrderCommand.CreateOrder(orderId, userId, details, replyTo),
                timeout,
                actorSystem.scheduler());
        return unwrap(stage);
    }

    public CompletionStage<OrderState> cancelOrder(UUID orderId,
                                                   UUID userId,
                                                   String reason) {
        log.info("OrderGateway#cancel orderId={} userId={} reason={}", orderId, userId, reason);
        ActorRef<OrderCommand> actor = actorFor(orderId);
        CompletionStage<StatusReply<OrderState>> stage = AskPattern.ask(
                actor,
                replyTo -> new OrderCommand.CancelOrder(orderId, userId, reason, replyTo),
                timeout,
                actorSystem.scheduler());
        return unwrap(stage);
    }

    public CompletionStage<OrderState> confirmStock(UUID orderId,
                                                    UUID inventoryId) {
        log.info("OrderGateway#confirmStock orderId={} inventoryId={}", orderId, inventoryId);
        ActorRef<OrderCommand> actor = actorFor(orderId);
        CompletionStage<StatusReply<OrderState>> stage = AskPattern.ask(
                actor,
                replyTo -> new OrderCommand.ConfirmStock(orderId, inventoryId, replyTo),
                timeout,
                actorSystem.scheduler());
        return unwrap(stage);
    }

    public CompletionStage<OrderState> markOutOfStock(UUID orderId, String reason) {
        log.warn("OrderGateway#outOfStock orderId={} reason={}", orderId, reason);
        ActorRef<OrderCommand> actor = actorFor(orderId);
        CompletionStage<StatusReply<OrderState>> stage = AskPattern.ask(
                actor,
                replyTo -> new OrderCommand.OutOfStock(orderId, reason, replyTo),
                timeout,
                actorSystem.scheduler());
        return unwrap(stage);
    }

    public CompletionStage<OrderState> markPaid(UUID orderId, UUID userId) {
        log.info("OrderGateway#markPaid orderId={} userId={}", orderId, userId);
        ActorRef<OrderCommand> actor = actorFor(orderId);
        CompletionStage<StatusReply<OrderState>> stage = AskPattern.ask(
                actor,
                replyTo -> new OrderCommand.MarkPaid(orderId, userId, replyTo),
                timeout,
                actorSystem.scheduler());
        return unwrap(stage);
    }

    public CompletionStage<OrderState> getState(UUID orderId) {
        log.info("OrderGateway#getState orderId={}", orderId);
        ActorRef<OrderCommand> actor = actorFor(orderId);
        return AskPattern.ask(
                actor,
                replyTo -> new OrderCommand.GetState(orderId, replyTo),
                timeout,
                actorSystem.scheduler());
    }

    private ActorRef<OrderCommand> actorFor(UUID id) {
        return orderActors.computeIfAbsent(id,
                key -> actorSystem.systemActorOf(
                        akka.actor.typed.javadsl.Behaviors.supervise(
                                        OrderBehavior.create(key, outboxPublisher))
                                .onFailure(SupervisorStrategy.restart()),
                        "order-" + key,
                        Props.empty()));
    }

    private static <T> CompletionStage<T> unwrap(CompletionStage<StatusReply<T>> stage) {
        CompletableFuture<T> result = new CompletableFuture<>();
        stage.whenComplete((reply, error) -> {
            if (error != null) {
                result.completeExceptionally(error);
            } else if (reply.isError()) {
                result.completeExceptionally(new IllegalStateException(reply.getError()));
            } else {
                result.complete(reply.getValue());
            }
        });
        return result;
    }

}
