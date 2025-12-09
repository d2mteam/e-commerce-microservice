package com.project.event_sourcing_core.domain;

public interface AggregateTypeMapper {
    Class<? extends Aggregate> getClassByAggregateType(String aggregateType);
}
