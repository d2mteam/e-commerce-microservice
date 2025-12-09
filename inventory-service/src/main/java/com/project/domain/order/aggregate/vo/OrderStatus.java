package com.project.domain.order.aggregate.vo;

public enum OrderStatus {
    CREATED,
    CONFIRMED,
    OUT_OF_STOCK,
    SHIPPING,
    DELIVERED,
    PAID,
    CANCELLED,
}