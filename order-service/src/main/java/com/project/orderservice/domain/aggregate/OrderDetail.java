package com.project.orderservice.domain.aggregate;

import lombok.Getter;

import java.util.UUID;

@Getter
public class OrderDetail {
    private UUID productId;
    private int quantity;
    private double unitPrice;
}
