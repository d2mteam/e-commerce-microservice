package com.project.infrastructure.jpa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "outbox_event")
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @NotNull
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @NotNull
    @Column(name = "payload", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> payload;

    @Builder.Default
    @NotNull
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    public void markSent() {
        this.status = Status.SENT;
    }

    public void markRetry(String errorMessage) {
        this.status = Status.RETRYING;
        this.retryCount += 1;
    }

    public void markFailed(String errorMessage) {
        this.status = Status.FAILED;
    }

    public enum Status {
        PENDING,
        RETRYING,
        SENT,
        FAILED
    }
}
