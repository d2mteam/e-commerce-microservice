package com.project.orderservice.service;

import com.project.orderservice.domain.outbox.OutboxEvent;
import com.project.orderservice.repository.OutboxEventRepository;
import com.project.orderservice.ultils.EventRoutingMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor
@EnableScheduling
public class EventProducerService {
    private final OutboxEventRepository outboxEventRepository;

    private final RestTemplate restTemplate;

    @Scheduled(fixedDelay = 10000)
    public void processPendingEvents() {

        System.out.println("Processing pending events per min");

        List<UUID> aggregates = outboxEventRepository.findAggregateIdsWithPendingEvents();
        if (aggregates.isEmpty()) return;

        for (UUID aggId : aggregates) {
            processAggregate(aggId);
        }
    }

    protected void processAggregate(UUID aggregateId) {
        List<OutboxEvent> batch = outboxEventRepository.findNextBatchForAggregate(aggregateId, 10);
        if (batch.isEmpty()) return;

        for (OutboxEvent event : batch) {
            try {
                String url = EventRoutingMapper.demo.getRouting().get(event.getEventType()).getFirst();
                sendEvent(url, event.getPayload());
            } catch (Exception ex) {
                int nextRetry = event.getRetryCount() + 1;
                outboxEventRepository.markFailed(event.getId(), ex.getMessage(), nextRetry, -1);
                break;
            }
        }
    }


    public void sendEvent(String url, String payloadJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payloadJson, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(url, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("HTTP error: " + response.getStatusCode());
        }
    }
}
