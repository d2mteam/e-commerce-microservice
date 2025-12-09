package com.project.application.integration.mapper;

import com.project.application.integration.IntegrationEvent;
import com.project.event_sourcing_core.domain.event.Event;

import java.util.List;

public interface DomainToIntegrationEventMapper<T extends Event> {
    boolean supports(Class<?> eventClass);
    List<IntegrationEvent> map(T event);
}