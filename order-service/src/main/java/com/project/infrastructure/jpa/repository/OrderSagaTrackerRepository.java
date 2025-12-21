package com.project.infrastructure.jpa.repository;

import com.project.infrastructure.jpa.entity.OrderSagaTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderSagaTrackerRepository extends JpaRepository<OrderSagaTracker, Long> {
    Optional<OrderSagaTracker> findByOrderIdAndStage(UUID orderId, OrderSagaTracker.Stage stage);
}
