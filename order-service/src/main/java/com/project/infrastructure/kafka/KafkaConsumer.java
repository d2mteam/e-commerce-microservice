package com.project.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.application.integration.IntegrationEvent;
import com.project.application.integration.Wrapper;
import com.project.application.integration.mapper.IntegrationEventMapper;
import com.project.application.service.LocalEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ObjectMapper objectMapper;
    private final IntegrationEventMapper integrationEventMapper;
    private final LocalEventPublisher localEventPublisher;

    @Transactional
    @KafkaListener(topics = "order-topic", containerFactory = "batchFactory")
    public void consumeBatch(List<String> messages, Acknowledgment acknowledgment) {
        log.info("Received {} messages", messages.size());

        for (String message : messages) {
            try {
                Wrapper wrapper = objectMapper.readValue(message, Wrapper.class);
                Class<? extends IntegrationEvent> clazz =
                        integrationEventMapper.getClassByIntegrationEventTypeMapper(wrapper.getEventType());
                IntegrationEvent event = objectMapper.convertValue(wrapper.getData(), clazz);
                localEventPublisher.publish(event);
                log.info("Received event: {}", event.getEventType());
            } catch (DataIntegrityViolationException ex) {
                log.error("Duplicated event {}", ex.getMessage(), ex);
            } catch (Exception ex) {
                log.error("Error while processing kafka message {}", ex.getMessage(), ex);
                throw new RuntimeException("Failed to process kafka message " + ex.getMessage(), ex);
            }
        }
//        acknowledgment.acknowledge();
    }
}
