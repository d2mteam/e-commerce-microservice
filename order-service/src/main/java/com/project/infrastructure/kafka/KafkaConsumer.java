package com.project.infrastructure.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.application.integration.IntegrationEvent;
import com.project.application.integration.Wrapper;
import com.project.application.integration.impl.ProductReleaseReply;
import com.project.application.integration.impl.ProductReleaseRequest;
import com.project.application.integration.impl.ProductReserveReply;
import com.project.application.integration.impl.ProductReserveRequest;
import com.project.application.integration.mapper.IntegrationEventMapper;
import com.project.application.service.LocalEventPublisher;
import com.project.infrastructure.jpa.entity.InboxEvent;
import com.project.infrastructure.jpa.repository.InboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ObjectMapper objectMapper;
    private final IntegrationEventMapper integrationEventMapper;
    private final LocalEventPublisher localEventPublisher;
    private final InboxEventRepository inboxEventRepository;

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
                persistInbox(event);
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

    private void persistInbox(IntegrationEvent event) {
        UUID aggregateId = extractAggregateId(event);
        if (aggregateId == null) {
            log.warn("Skip inbox persistence because aggregateId is missing for event type {}", event.getEventType());
            return;
        }

        Map<String, Object> payload = objectMapper.convertValue(event, new TypeReference<>() {
        });
        InboxEvent inboxEvent = InboxEvent.builder()
                .aggregateId(aggregateId)
                .payload(payload)
                .build();
        inboxEventRepository.save(inboxEvent);
    }

    private UUID extractAggregateId(IntegrationEvent event) {
        if (event instanceof ProductReserveReply reply) {
            return reply.getOrderId();
        }
        if (event instanceof ProductReleaseReply reply) {
            return reply.getOrderId();
        }
        if (event instanceof ProductReserveRequest request) {
            return request.getOrderId();
        }
        if (event instanceof ProductReleaseRequest request) {
            return request.getOrderId();
        }
        return null;
    }
}
