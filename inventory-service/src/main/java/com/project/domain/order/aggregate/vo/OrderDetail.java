package com.project.domain.order.aggregate.vo;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OrderDetail {
    private UUID productId;
    private int quantity;
}