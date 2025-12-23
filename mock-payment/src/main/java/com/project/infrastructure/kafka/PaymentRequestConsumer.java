package com.project.infrastructure.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.integration.message.PaymentRequested;
import com.project.service.PaymentService;
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
public class PaymentRequestConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    @KafkaListener(topics = "payment-service", groupId = "payment-service--1221")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            Map<String, Object> wrapper = objectMapper.readValue(record.value(), new TypeReference<>() {});
            String eventType = (String) wrapper.get("eventType");
            Map<String, Object> data = (Map<String, Object>) wrapper.get("data");

            if (PaymentRequested.class.getSimpleName().equals(eventType)) {
                PaymentRequested req = objectMapper.convertValue(data, PaymentRequested.class);
                paymentService.createInvoice(req);
                log.info("Payment invoice created orderId={} amount={}", req.orderId(), req.amount());
            } else {
                log.warn("Unknown eventType {}", eventType);
            }
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("PaymentRequestConsumer failed for message {}", record.value(), ex);
            throw new RuntimeException(ex);
        }
    }
}
