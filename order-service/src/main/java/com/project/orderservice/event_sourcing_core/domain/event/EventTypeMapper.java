package com.project.orderservice.event_sourcing.domain.event;

public interface EventTypeMapper {

    Class<? extends Event> getClassByEventType(String eventType);
}
