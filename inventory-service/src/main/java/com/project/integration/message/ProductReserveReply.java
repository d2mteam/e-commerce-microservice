package com.project.integration.message;

import java.util.UUID;

public record ProductReserveReply(
        UUID orderId,
        UUID productId,
        Result result,
        String reason,
        String correlationId
) {
    public enum Result {
        SUCCESS, FAILURE
    }
}
