package com.project.akka.order;

import com.project.akka.serialization.CborSerializable;
import com.project.domain.order.aggregate.vo.OrderDetail;
import com.project.domain.order.aggregate.vo.OrderStatus;
import lombok.Builder;
import lombok.Singular;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder(toBuilder = true)
public record OrderState(
        UUID orderId,
        UUID userId,
        @Singular("detail") List<OrderDetail> orderDetails,
        OrderStatus status,
        UUID inventoryId,
        OffsetDateTime createdAt,
        OffsetDateTime paidAt
) implements CborSerializable {

    public static OrderState empty(UUID orderId) {
        return OrderState.builder()
                .orderId(orderId)
                .orderDetails(new ArrayList<>())
                .status(OrderStatus.CREATED)
                .build();
    }
}
