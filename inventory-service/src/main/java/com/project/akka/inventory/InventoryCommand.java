package com.project.akka.inventory;

import akka.pattern.StatusReply;
import akka.actor.typed.ActorRef;
import com.project.akka.serialization.CborSerializable;

import java.util.UUID;

public sealed interface InventoryCommand extends CborSerializable
        permits InventoryCommand.CreateInventory,
                InventoryCommand.AddStock,
                InventoryCommand.ReserveStock,
                InventoryCommand.CancelReservation,
                InventoryCommand.ReleaseStock,
                InventoryCommand.GetState {

    record CreateInventory(UUID inventoryId,
                           String sku,
                           int initialQuantity,
                           ActorRef<StatusReply<InventoryState>> replyTo) implements InventoryCommand {
    }

    record AddStock(UUID inventoryId,
                    int quantity,
                    ActorRef<StatusReply<InventoryState>> replyTo) implements InventoryCommand {
    }

    record ReserveStock(UUID inventoryId,
                        UUID orderId,
                        int quantity,
                        String correlationId,
                        ActorRef<StatusReply<InventoryState>> replyTo) implements InventoryCommand {
    }

    record CancelReservation(UUID inventoryId,
                             UUID orderId,
                             ActorRef<StatusReply<InventoryState>> replyTo) implements InventoryCommand {
    }

    record ReleaseStock(UUID inventoryId,
                        UUID orderId,
                        ActorRef<StatusReply<InventoryState>> replyTo) implements InventoryCommand {
    }

    record GetState(UUID inventoryId,
                    ActorRef<InventoryState> replyTo) implements InventoryCommand {
    }
}
