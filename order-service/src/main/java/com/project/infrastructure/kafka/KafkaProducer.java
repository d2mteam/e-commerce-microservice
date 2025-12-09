package com.project.infrastructure.kafka;

import com.project.infrastructure.jpa.entity.OutboxEvent;
import com.project.infrastructure.jpa.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {
    private final OutboxEventRepository outboxEventRepository;
    private final OutboxService outboxService;

    private static final int MAX_RETRY = 5;

    @Scheduled(fixedDelay = 5000)
    void sendEvents() {
        Page<OutboxEvent> events = outboxEventRepository.findByStatusInOrderByIdAsc(
                List.of(OutboxEvent.Status.PENDING, OutboxEvent.Status.RETRYING),
                PageRequest.of(0, 100)
        );

        if (events.isEmpty()) {
            return;
        }

        log.info("KafkaProducer: processing {} outbox events", events.getContent().size());

        for (OutboxEvent event : events.getContent()) {
            try {
                outboxService.sendEvent(event);
                outboxService.markSent(event);
            } catch (Exception ex) {
                log.error("KafkaProducer: failed to send outbox event id={}, retryCount={}, error={}",
                        event.getId(), event.getRetryCount(), ex.getMessage(), ex);

                if (event.getRetryCount() + 1 >= MAX_RETRY) {
                    outboxService.markFailed(event, ex.getMessage());
                } else {
                    outboxService.markRetry(event, ex.getMessage());
                }
            }
        }
    }
}
