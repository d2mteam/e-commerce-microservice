package com.project.infrastructure.kafka;

import com.project.application.integration.Wrapper;
import com.project.infrastructure.jpa.entity.OutboxEvent;
import com.project.infrastructure.jpa.repository.OutboxEventRepository;
import com.project.ultils.TopicMapper;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@AllArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final TopicMapper topicMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void sendEvent(OutboxEvent outboxEvent) {

        List<String> topics = topicMapper.getTopicsFromEventType(outboxEvent.getEventType());

        Wrapper wrapper = Wrapper.builder()
                .eventType(outboxEvent.getEventType())
                .data(outboxEvent.getPayload())
                .build();

        for (String topic : topics) {
            kafkaTemplate.send(topic, outboxEvent.getAggregateId().toString(), wrapper);
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSent(OutboxEvent outboxEvent) {
        outboxEvent.markSent();
        outboxEventRepository.save(outboxEvent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(OutboxEvent outboxEvent, String reason) {
        outboxEvent.markFailed(reason);
        outboxEventRepository.save(outboxEvent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRetry(OutboxEvent outboxEvent, String reason) {
        outboxEvent.markRetry(reason);
        outboxEventRepository.save(outboxEvent);
    }
}
