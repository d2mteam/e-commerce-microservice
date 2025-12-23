package com.project.integration.message;

import java.util.UUID;

public record PaymentResult(
        UUID orderId,
        UUID userId,
        Status status,
        String reason,
        String correlationId
) {
    public enum Status { SUCCESS, FAILED }
}
