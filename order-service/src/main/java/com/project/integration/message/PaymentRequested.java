package com.project.integration.message;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequested(
        UUID orderId,
        UUID userId,
        BigDecimal amount,
        String correlationId
) {
}
