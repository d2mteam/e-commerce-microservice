package com.project.infrastructure.jpa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Getter
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "inbox_event")
public class InboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

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
    @Column(name = "status", nullable = false)
    private Status status = Status.RECEIVED;

    public enum Status {
        RECEIVED,
        PROCESSED,
        FAILED,
        RETRYING
    }

    public void markProcessed() {
        this.status = Status.PROCESSED;
    }

    public void markRetry(String errorMessage) {
        this.status = Status.RETRYING;
        this.retryCount += 1;
    }

    public void markFailed(String errorMessage) {
        this.status = Status.FAILED;
    }

}