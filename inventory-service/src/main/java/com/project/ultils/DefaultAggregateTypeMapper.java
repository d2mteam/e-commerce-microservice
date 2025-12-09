package com.project.ultils;

import com.project.domain.inventory.aggregate.InventoryAggregate;
import com.project.event_sourcing_core.domain.Aggregate;
import com.project.event_sourcing_core.domain.AggregateTypeMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DefaultAggregateTypeMapper implements AggregateTypeMapper {
    @Override
    public Class<? extends Aggregate> getClassByAggregateType(String aggregateType) {
        if (Objects.equals(aggregateType, InventoryAggregate.class.getSimpleName())) {
            return InventoryAggregate.class;
        }
        throw new IllegalArgumentException("Unsupported aggregate type: " + aggregateType);
    }
}
