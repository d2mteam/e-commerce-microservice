package com.project.event_sourcing_core.domain.event;

public interface EventTypeMapper {
    Class<? extends Event> getClassByEventType(String eventType);
}
