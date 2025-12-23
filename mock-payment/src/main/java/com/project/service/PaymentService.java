package com.project.service;

import com.project.domain.model.PaymentInvoice;
import com.project.integration.message.PaymentRequested;
import com.project.integration.message.PaymentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class PaymentService {

    private final PaymentInvoiceRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AtomicLong expirySeconds;

    @Autowired
    public PaymentService(PaymentInvoiceRepository repository,
                          KafkaTemplate<String, Object> kafkaTemplate,
                          @Value("${payment.expiry-seconds:60}") long expirySeconds) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.expirySeconds = new AtomicLong(expirySeconds);
    }

    @Transactional
    public PaymentInvoice createInvoice(PaymentRequested req) {
        return repository.findById(req.orderId())
                .orElseGet(() -> {
                    PaymentInvoice invoice = PaymentInvoice.builder()
                            .orderId(req.orderId())
                            .userId(req.userId())
                            .amount(req.amount() != null ? req.amount() : BigDecimal.ZERO)
                            .correlationId(req.correlationId())
                            .expiresAt(OffsetDateTime.now().plusSeconds(expirySeconds.get()))
                            .status(PaymentInvoice.Status.PENDING)
                            .build();
                    return repository.save(invoice);
                });
    }
    @Transactional
    public PaymentInvoice markPaid(UUID orderId) {
        PaymentInvoice invoice = repository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        if (invoice.getStatus() == PaymentInvoice.Status.SUCCESS) {
            return invoice;
        }
        invoice.setStatus(PaymentInvoice.Status.SUCCESS);
        repository.save(invoice);
        emitResult(invoice, PaymentResult.Status.SUCCESS, null);
        return invoice;
    }

    @Transactional
    public void expirePending() {
        List<PaymentInvoice> pending = repository.findByStatusAndExpiresAtBefore(
                PaymentInvoice.Status.PENDING, OffsetDateTime.now());
        for (PaymentInvoice invoice : pending) {
            invoice.setStatus(PaymentInvoice.Status.FAILED);
            repository.save(invoice);
            emitResult(invoice, PaymentResult.Status.FAILED, "TIMEOUT");
            log.info("Payment expired orderId={}", invoice.getOrderId());
        }
    }

    public void updateExpirySeconds(long seconds) {
        expirySeconds.set(seconds);
    }

    private void emitResult(PaymentInvoice invoice, PaymentResult.Status status, String reason) {
        var result = new PaymentResult(
                invoice.getOrderId(),
                invoice.getUserId(),
                status,
                reason,
                invoice.getCorrelationId() != null ? invoice.getCorrelationId() : invoice.getOrderId().toString()
        );
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("orderId", result.orderId());
        payload.put("userId", result.userId());
        payload.put("status", result.status().name());
        if (result.reason() != null) {
            payload.put("reason", result.reason());
        }
        payload.put("correlationId", result.correlationId());
        kafkaTemplate.send("order-service", invoice.getOrderId().toString(),
                Map.of("eventType", PaymentResult.class.getSimpleName(), "data", payload));
    }
}
