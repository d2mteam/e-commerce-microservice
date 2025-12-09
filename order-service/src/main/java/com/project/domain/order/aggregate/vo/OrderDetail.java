package com.project.domain.order.aggregate.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderDetail {
    private UUID productId;
    private int quantity;
    private BigDecimal price;
}