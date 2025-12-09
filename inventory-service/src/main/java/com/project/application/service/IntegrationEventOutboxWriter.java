package com.project.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.application.integration.IntegrationEvent;
import com.project.infrastructure.jpa.entity.OutboxEvent;
import com.project.infrastructure.jpa.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IntegrationEventOutboxWriter {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void persist(UUID aggregateId, IntegrationEvent integrationEvent) {
        Map<String, Object> payload = objectMapper.convertValue(integrationEvent, new TypeReference<>() {
        });

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateId(aggregateId)
                .eventType(integrationEvent.getEventType())
                .payload(payload)
                .build();

        outboxEventRepository.save(outboxEvent);
    }
}
