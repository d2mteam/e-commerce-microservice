package com.project.infrastructure.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.akka.inventory.InventoryGateway;
import com.project.integration.message.ProductReleaseRequest;
import com.project.integration.message.ProductReserveRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntegrationMessageConsumer {

    private final ObjectMapper objectMapper;
    private final InventoryGateway inventoryGateway;
    private final MessageDeduplicator deduplicator;

    @KafkaListener(topics = "inventory-service", groupId = "inventory-service", concurrency = "1")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            Map<String, Object> wrapper = objectMapper.readValue(record.value(), new TypeReference<>() {});
            String eventType = (String) wrapper.get("eventType");
            Map<String, Object> data = (Map<String, Object>) wrapper.get("data");

            String correlationId = data != null ? (String) data.get("correlationId") : null;
            String idemKey = eventType + ":" + (correlationId != null ? correlationId
                    : record.topic() + "-" + record.partition() + "-" + record.offset());

            // ghi nhận idempotency; nếu trùng thì chỉ ack và bỏ qua
            boolean firstSeen = deduplicator.tryMark(idemKey, record.topic(), record.partition(), record.offset());
            if (!firstSeen) {
                ack.acknowledge();
                return;
            }

            if (ProductReserveRequest.class.getSimpleName().equals(eventType)) {
                ProductReserveRequest req = objectMapper.convertValue(data, ProductReserveRequest.class);
                inventoryGateway.reserve(req.productId(), req.orderId(), req.quantity(), req.correlationId())
                        .toCompletableFuture()
                        .join();
                log.info("Handled ProductReserveRequest order={} product={}", req.orderId(), req.productId());
                return;
            }

            if (ProductReleaseRequest.class.getSimpleName().equals(eventType)) {
                ProductReleaseRequest req = objectMapper.convertValue(data, ProductReleaseRequest.class);
                inventoryGateway.release(req.productId(), req.orderId())
                        .toCompletableFuture()
                        .join();
                log.info("Handled ProductReleaseRequest order={} product={}", req.orderId(), req.productId());
                return;
            }

            log.warn("Unknown integration event type: {}", eventType);

        } catch (Exception ex) {
            log.error("Failed to process kafka message: {}", record.value(), ex);
            // không ack để Kafka retry
            throw new RuntimeException(ex);
        }
        ack.acknowledge();
    }
}
