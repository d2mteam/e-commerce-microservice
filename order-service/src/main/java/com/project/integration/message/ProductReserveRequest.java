package com.project.integration.message;

import java.util.UUID;

public record ProductReserveRequest(
        UUID orderId,
        UUID productId,
        int quantity,
        String correlationId
) {
}
