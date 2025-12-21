package com.project.akka.inventory;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AskPattern;
import akka.pattern.StatusReply;
import com.project.integration.IntegrationOutboxPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryGateway {

    private final ActorSystem<Void> actorSystem;
    private final IntegrationOutboxPublisher outboxPublisher;

    private final Map<UUID, ActorRef<InventoryCommand>> inventoryActors = new ConcurrentHashMap<>();
    private final Duration timeout = Duration.ofSeconds(3);

    public CompletionStage<InventoryState> createInventory(UUID id, String sku, int initialQuantity) {
        log.info("InventoryGateway#create id={} sku={} initialQty={}", id, sku, initialQuantity);
        var actor = actorFor(id);
        CompletionStage<StatusReply<InventoryState>> stage = AskPattern.ask(
                actor,
                replyTo -> new InventoryCommand.CreateInventory(id, sku, initialQuantity, replyTo),
                timeout,
                actorSystem.scheduler());
        return unwrap(stage);
    }

    public CompletionStage<InventoryState> addStock(UUID id, int quantity) {
        log.info("InventoryGateway#addStock id={} quantity={}", id, quantity);
        var actor = actorFor(id);
        CompletionStage<StatusReply<InventoryState>> stage = AskPattern.ask(
                actor,
                replyTo -> new InventoryCommand.AddStock(id, quantity, replyTo),
                timeout,
                actorSystem.scheduler());
        return unwrap(stage);
    }

    public CompletionStage<InventoryState> reserve(UUID id, UUID orderId, int quantity, String correlationId) {
        log.info("InventoryGateway#reserve id={} orderId={} quantity={} correlationId={}", id, orderId, quantity, correlationId);
        var actor = actorFor(id);
        CompletionStage<StatusReply<InventoryState>> stage = AskPattern.ask(
                actor,
                replyTo -> new InventoryCommand.ReserveStock(id, orderId, quantity, correlationId, replyTo),
                timeout,
                actorSystem.scheduler());
        return unwrap(stage);
    }

    public CompletionStage<InventoryState> release(UUID id, UUID orderId) {
        log.info("InventoryGateway#release id={} orderId={}", id, orderId);
        var actor = actorFor(id);
        CompletionStage<StatusReply<InventoryState>> stage = AskPattern.ask(
                actor,
                replyTo -> new InventoryCommand.ReleaseStock(id, orderId, replyTo),
                timeout,
                actorSystem.scheduler());
        return unwrap(stage);
    }

    public CompletionStage<InventoryState> cancelReservation(UUID id, UUID orderId) {
        log.info("InventoryGateway#cancelReservation id={} orderId={}", id, orderId);
        var actor = actorFor(id);
        CompletionStage<StatusReply<InventoryState>> stage = AskPattern.ask(
                actor,
                replyTo -> new InventoryCommand.CancelReservation(id, orderId, replyTo),
                timeout,
                actorSystem.scheduler());
        return unwrap(stage);
    }

    public CompletionStage<InventoryState> getState(UUID id) {
        log.info("InventoryGateway#getState id={}", id);
        var actor = actorFor(id);
        return AskPattern.ask(
                actor,
                replyTo -> new InventoryCommand.GetState(id, replyTo),
                timeout,
                actorSystem.scheduler());
    }

    private ActorRef<InventoryCommand> actorFor(UUID id) {
        return inventoryActors.computeIfAbsent(id,
                key -> actorSystem.systemActorOf(
                        akka.actor.typed.javadsl.Behaviors.supervise(
                                        InventoryBehavior.create(key, outboxPublisher))
                                .onFailure(SupervisorStrategy.restart()),
                        "inventory-" + key,
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
