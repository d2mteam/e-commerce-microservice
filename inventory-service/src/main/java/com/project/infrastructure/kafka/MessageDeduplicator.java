package com.project.infrastructure.kafka;

import com.project.infrastructure.jpa.entity.ProcessedMessage;
import com.project.infrastructure.jpa.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageDeduplicator {

    private final ProcessedMessageRepository repository;

    /**
     * @return true nếu ghi nhận mới, false nếu đã tồn tại.
     */
    public boolean tryMark(String idempotencyKey, String topic, int partition, long offset) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return false;
        }
        try {
            repository.save(ProcessedMessage.builder()
                    .idempotencyKey(idempotencyKey)
                    .topic(topic)
                    .partition(partition)
                    .offset(offset)
                    .build());
            return true;
        } catch (DataIntegrityViolationException dup) {
            log.debug("Skip duplicate message key={} topic={} partition={} offset={} (idempotent)",
                    idempotencyKey, topic, partition, offset);
            return false;
        }
    }
}
