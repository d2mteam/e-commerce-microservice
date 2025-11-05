package com.project.orderservice.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderDetail {
    private String productId;
    private int quantity;
    private double price;
}