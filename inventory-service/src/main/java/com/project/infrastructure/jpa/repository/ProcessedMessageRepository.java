package com.project.infrastructure.jpa.repository;

import com.project.infrastructure.jpa.entity.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);
}
