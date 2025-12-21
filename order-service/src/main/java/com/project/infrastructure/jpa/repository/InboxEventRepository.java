package com.project.infrastructure.jpa.repository;

import com.project.infrastructure.jpa.entity.InboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxEventRepository extends JpaRepository<InboxEvent, Long> {
}
