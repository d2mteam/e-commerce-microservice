package com.project.orderservice.domain.outbox;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class OutboxEvent {
    private Long id;
    private UUID aggregateId;
    private String aggregateType;
    private String eventType;
    private Integer eventVersion;
    private String payload;
    private Instant createdAt;
    private Instant sentAt;
    private int retryCount;
    private String status;     // PENDING | SENT | FAILED | RETRYING
    private String lastError;
}