package com.project.infrastructure.jpa.entity;

import com.project.application.integration.impl.ProductReleaseReply;
import com.project.application.integration.impl.ProductReserveReply;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_saga_tracker", uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "stage"}))
public class OrderSagaTracker {

    public enum Stage {
        RESERVATION,
        RELEASE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false, length = 30)
    private Stage stage;

    @Column(name = "user_id")
    private UUID userId;

    @NotNull
    @Column(name = "expected_replies", nullable = false)
    private Integer expectedReplies;

    @Builder.Default
    @NotNull
    @Column(name = "received_replies", nullable = false)
    private Integer receivedReplies = 0;

    @Builder.Default
    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "processed_products")
    private Set<UUID> processedProducts = new HashSet<>();

    @Builder.Default
    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "failure_reasons")
    private List<String> failureReasons = new ArrayList<>();

    @Column(name = "confirmation_reference")
    private UUID confirmationReference;

    @Builder.Default
    @NotNull
    @Column(name = "finalized", nullable = false)
    private Boolean finalized = false;

    @Version
    private Long version;

    public static OrderSagaTracker newReservation(UUID orderId, UUID userId, int expectedReplies) {
        return OrderSagaTracker.builder()
                .orderId(orderId)
                .userId(userId)
                .expectedReplies(expectedReplies)
                .stage(Stage.RESERVATION)
                .build();
    }

    public static OrderSagaTracker newRelease(UUID orderId, int expectedReplies) {
        return OrderSagaTracker.builder()
                .orderId(orderId)
                .expectedReplies(expectedReplies)
                .stage(Stage.RELEASE)
                .build();
    }

    public boolean recordReservation(ProductReserveReply reply) {
        if (Boolean.TRUE.equals(finalized)) {
            return false;
        }

        Set<UUID> products = new HashSet<>(safeProcessedProducts());
        if (!products.add(reply.getProductId())) {
            return false;
        }
        this.processedProducts = products;

        this.receivedReplies = receivedReplies + 1;

        if (reply.getResult() == ProductReserveReply.Result.FAILURE) {
            List<String> reasons = new ArrayList<>(safeFailureReasons());
            String reason = reply.getReason() == null ? "UNKNOWN_REASON" : reply.getReason();
            reasons.add(reason);
            this.failureReasons = reasons;
        } else if (confirmationReference == null) {
            this.confirmationReference = reply.getProductId();
        }
        return true;
    }

    public boolean recordRelease(ProductReleaseReply reply) {
        if (Boolean.TRUE.equals(finalized)) {
            return false;
        }

        Set<UUID> products = new HashSet<>(safeProcessedProducts());
        if (!products.add(reply.getProductId())) {
            return false;
        }
        this.processedProducts = products;

        this.receivedReplies = receivedReplies + 1;

        if (reply.getResult() == ProductReleaseReply.Result.FAILURE) {
            List<String> reasons = new ArrayList<>(safeFailureReasons());
            String reason = reply.getReason() == null ? "UNKNOWN_REASON" : reply.getReason();
            reasons.add(reason);
            this.failureReasons = reasons;
        }
        return true;
    }

    public boolean markCompletedIfReady() {
        if (Boolean.TRUE.equals(finalized)) {
            return false;
        }
        if (receivedReplies >= expectedReplies) {
            finalized = true;
            return true;
        }
        return false;
    }

    public boolean isSuccessful() {
        return safeFailureReasons().isEmpty();
    }

    public UUID confirmationReferenceOrDefault(UUID fallback) {
        return confirmationReference != null ? confirmationReference : fallback;
    }

    private Set<UUID> safeProcessedProducts() {
        if (processedProducts == null) {
            processedProducts = new HashSet<>();
        }
        return processedProducts;
    }

    private List<String> safeFailureReasons() {
        if (failureReasons == null) {
            failureReasons = new ArrayList<>();
        }
        return failureReasons;
    }
}
