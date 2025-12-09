package com.project.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.application.integration.IntegrationEvent;

import com.project.application.integration.Wrapper;
import com.project.application.integration.mapper.IntegrationEventMapper;
import com.project.application.service.LocalEventPublisher;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class KafkaConsumer {
    private final ObjectMapper objectMapper;
    private final IntegrationEventMapper integrationEventMapper;
    private final LocalEventPublisher localEventPublisher;

    @Transactional
    @KafkaListener(topics = "inventory-topic", containerFactory = "batchFactory")
    public void consumeBatch(List<String> messages, Acknowledgment ack) {
        log.info("Received {} messages", messages.size());

        for (String message : messages) {
            try {
                Wrapper wrapper = objectMapper.readValue(message, Wrapper.class);
                String eventType = wrapper.getEventType();
                Class<? extends IntegrationEvent> clazz =
                        integrationEventMapper.getClassByIntegrationEventTypeMapper(eventType);
                IntegrationEvent event =
                        objectMapper.convertValue(wrapper.getData(), clazz);

                localEventPublisher.publish(event);

                log.info("Received event: {}", event.getEventType());

            } catch (DataIntegrityViolationException e) {
                log.error("Duplicated event {}", e.getMessage(), e);
            } catch (Exception e) {
                log.error("Error while processing kafka message {}", e.getMessage(), e);
                throw new RuntimeException("Something went wrong while processing kafka message " + e.getMessage());
            }
        }
//        ack.acknowledge();
    }
}
