package com.project.orderservice.service;

import com.project.orderservice.domain.outbox.OutboxEvent;
import com.project.orderservice.repository.OutboxEventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor
@EnableScheduling
public class EventProducerService {
    private final OutboxEventRepository repo;


    @Scheduled(fixedDelay = 1000)
    public void processPendingEvents() {

        System.out.println("Processing pending events per min");

        List<UUID> aggregates = repo.findAggregateIdsWithPendingEvents();
        if (aggregates.isEmpty()) return;

        for (UUID aggId : aggregates) {
            processAggregate(aggId);
        }
    }

    private void processAggregate(UUID aggregateId) {
        List<OutboxEvent> batch = repo.findNextBatchForAggregate(aggregateId, 10);
        if (batch.isEmpty()) return;

        for (OutboxEvent event : batch) {
            try {

            } catch (Exception ex) {
                int nextRetry = event.getRetryCount() + 1;
                repo.markFailed(event.getId(), ex.getMessage(), nextRetry, 10);
                break;
            }
        }
    }
}
