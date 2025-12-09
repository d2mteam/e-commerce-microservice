package com.project.ultils;

import com.project.domain.order.aggregate.OrderAggregate;
import com.project.event_sourcing_core.domain.Aggregate;
import com.project.event_sourcing_core.domain.AggregateTypeMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DefaultAggregateTypeMapper implements AggregateTypeMapper {
    @Override
    public Class<? extends Aggregate> getClassByAggregateType(String aggregateType) {
        if (Objects.equals(aggregateType, OrderAggregate.class.getSimpleName())) {
            return OrderAggregate.class;
        }
        throw new IllegalArgumentException("Unsupported aggregate type: " + aggregateType);
    }
}
