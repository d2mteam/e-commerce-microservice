package com.project.infrastructure.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.akka.order.OrderGateway;
import com.project.integration.message.PaymentResult;
import com.project.integration.message.ProductReleaseReply;
import com.project.integration.message.ProductReserveReply;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntegrationMessageConsumer {

    private final ObjectMapper objectMapper;
    private final OrderGateway orderGateway;
    private final MessageDeduplicator deduplicator;

    @KafkaListener(topics = "order-service", groupId = "order-service")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            Map<String, Object> wrapper = objectMapper.readValue(record.value(), new TypeReference<>() {});
            String eventType = (String) wrapper.get("eventType");
            Map<String, Object> data = (Map<String, Object>) wrapper.get("data");

            String correlationId = data != null ? (String) data.get("correlationId") : null;
            String idemKey = eventType + ":" + (correlationId != null ? correlationId
                    : record.topic() + "-" + record.partition() + "-" + record.offset());

            if (!deduplicator.tryMark(idemKey, record.topic(), record.partition(), record.offset())) {
                ack.acknowledge();
                return;
            }

            if (ProductReserveReply.class.getSimpleName().equals(eventType)) {
                ProductReserveReply reply = objectMapper.convertValue(data, ProductReserveReply.class);
                if (reply.result() == ProductReserveReply.Result.SUCCESS) {
                    orderGateway.confirmStock(reply.orderId(), reply.productId()).toCompletableFuture().join();
                    log.info("ConfirmStock by reserve success order={}, product={}", reply.orderId(), reply.productId());
                } else {
                    orderGateway.markOutOfStock(reply.orderId(), reply.reason() != null ? reply.reason() : "RESERVE_FAILED")
                            .toCompletableFuture().join();
                    log.info("MarkOutOfStock by reserve fail order={}, reason={}", reply.orderId(), reply.reason());
                }
                return;
            }

            if (ProductReleaseReply.class.getSimpleName().equals(eventType)) {
                ProductReleaseReply reply = objectMapper.convertValue(data, ProductReleaseReply.class);
                log.info("Release reply received order={} product={} result={}", reply.orderId(), reply.productId(), reply.result());
                ack.acknowledge();
                return;
            }

            if (PaymentResult.class.getSimpleName().equals(eventType)) {
                PaymentResult result = objectMapper.convertValue(data, PaymentResult.class);
                if (result.status() == PaymentResult.Status.SUCCESS) {
                    orderGateway.markPaid(result.orderId(), result.userId()).toCompletableFuture().join();
                    log.info("Payment SUCCESS order={}", result.orderId());
                } else {
                    orderGateway.cancelOrder(result.orderId(), result.userId(), result.reason() != null ? result.reason() : "PAYMENT_FAILED")
                            .toCompletableFuture().join();
                    log.info("Payment FAILED order={} reason={}", result.orderId(), result.reason());
                }
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
