package com.project.orderservice.dto;

import com.project.orderservice.domain.OrderDetail;
import com.project.orderservice.domain.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID orderId;
    private List<OrderDetail> orderDetails;
    private OrderStatus status;
}