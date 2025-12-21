package com.project.integration.message;

import java.util.UUID;

public record ProductReleaseRequest(
        UUID orderId,
        UUID productId,
        String correlationId
) {
}
