package com.project.application.integration.mapper;

import com.project.application.integration.IntegrationEvent;

public interface IntegrationEventMapper {
    Class<? extends IntegrationEvent> getClassByIntegrationEventTypeMapper(String type);
}
