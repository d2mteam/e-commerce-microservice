package com.project.infrastructure.jpa.repository;

import com.project.infrastructure.jpa.entity.InventorySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InventorySummaryRepository extends JpaRepository<InventorySummary, UUID> {
}
