package com.project.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.infrastructure.jpa.entity.OutboxEvent;
import com.project.infrastructure.jpa.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JpaIntegrationOutboxPublisher implements IntegrationOutboxPublisher {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public void save(UUID aggregateId, IntegrationMessage message) {
        try {
            Map<String, Object> payload = message.payload();
            if (payload == null) {
                payload = objectMapper.convertValue(message, new TypeReference<>() {
                });
            }

            OutboxEvent event = OutboxEvent.builder()
                    .aggregateId(aggregateId)
                    .eventType(message.type())
                    .payload(payload)
                    .build();
            repository.save(event);
        } catch (Exception e) {
            log.error("Failed to persist integration outbox message type={} aggregateId={}: {}",
                    message.type(), aggregateId, e.getMessage(), e);
        }
    }
}
