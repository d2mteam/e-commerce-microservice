package com.project.infrastructure.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_summary")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventorySummary {

    @Id
    @Column(name = "inventory_id")
    private UUID inventoryId;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "available_quantity")
    private int availableQuantity;

    @Column(name = "reserved_quantity")
    private int reservedQuantity;

    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;
}
