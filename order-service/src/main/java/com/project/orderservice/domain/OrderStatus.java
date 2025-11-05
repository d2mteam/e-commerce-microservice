package com.project.orderservice.domain;

public enum OrderStatus {
    CREATED,
    CONFIRMED,
    OUT_OF_STOCK,
    SHIPPING,
    DELIVERED,
    PAID,
    CANCELLED,
}