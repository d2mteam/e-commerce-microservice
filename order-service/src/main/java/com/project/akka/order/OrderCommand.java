package com.project.akka.order;

import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import com.project.akka.serialization.CborSerializable;
import com.project.domain.order.aggregate.vo.OrderDetail;

import java.util.List;
import java.util.UUID;

public sealed interface OrderCommand extends CborSerializable
        permits OrderCommand.CreateOrder,
                OrderCommand.ConfirmStock,
                OrderCommand.OutOfStock,
                OrderCommand.CancelOrder,
                OrderCommand.MarkPaid,
                OrderCommand.GetState {

    record CreateOrder(UUID orderId,
                       UUID userId,
                       List<OrderDetail> orderDetails,
                       ActorRef<StatusReply<OrderState>> replyTo) implements OrderCommand {
    }

    record ConfirmStock(UUID orderId,
                        UUID inventoryId,
                        ActorRef<StatusReply<OrderState>> replyTo) implements OrderCommand {
    }

    record OutOfStock(UUID orderId,
                      String reason,
                      ActorRef<StatusReply<OrderState>> replyTo) implements OrderCommand {
    }

    record CancelOrder(UUID orderId,
                       UUID userId,
                       String reason,
                       ActorRef<StatusReply<OrderState>> replyTo) implements OrderCommand {
    }

    record MarkPaid(UUID orderId,
                    UUID userId,
                    ActorRef<StatusReply<OrderState>> replyTo) implements OrderCommand {
    }

    record GetState(UUID orderId,
                    ActorRef<OrderState> replyTo) implements OrderCommand {
    }
}
