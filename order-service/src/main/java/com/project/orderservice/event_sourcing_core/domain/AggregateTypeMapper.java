package com.project.orderservice.event_sourcing.domain;

public interface AggregateTypeMapper {
    Class<? extends Aggregate> getClassByAggregateType(String aggregateType);
}
