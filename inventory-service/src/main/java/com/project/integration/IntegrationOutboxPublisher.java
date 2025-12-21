package com.project.integration;

import java.util.UUID;

public interface IntegrationOutboxPublisher {
    void save(UUID aggregateId, IntegrationMessage message);
}
