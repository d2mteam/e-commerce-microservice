package com.project.orderservice.dto;

import com.project.orderservice.domain.OrderDetail;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private List<OrderDetail> orderDetails;
}
