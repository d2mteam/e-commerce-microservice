package com.project.infrastructure.jpa.repository;

import com.project.infrastructure.jpa.entity.OutboxEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    Page<OutboxEvent> findByStatusInOrderByIdAsc(Collection<OutboxEvent.Status> statuses,
                                                 Pageable pageable);
}
