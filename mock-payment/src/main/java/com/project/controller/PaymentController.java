package com.project.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.domain.model.PaymentInvoice;
import com.project.service.PaymentInvoiceRepository;
import com.project.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentInvoiceRepository repository;
    private final ObjectMapper objectMapper;

    @PostMapping("/pay")
    public ResponseEntity<JsonNode> pay(@RequestBody JsonNode request) {
        UUID orderId = UUID.fromString(request.get("orderId").asText());
        try {
            PaymentInvoice invoice = paymentService.markPaid(orderId);
            var res = objectMapper.valueToTree(invoice);
            return ResponseEntity.ok(res);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(objectMapper.createObjectNode().put("error", ex.getMessage()));
        }
    }

    @PostMapping("/config/timeout")
    public ResponseEntity<JsonNode> updateTimeout(@RequestBody JsonNode request) {
        long seconds = request.get("seconds").asLong();
        paymentService.updateExpirySeconds(seconds);
        return ResponseEntity.ok(objectMapper.createObjectNode().put("expirySeconds", seconds));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<JsonNode> getInvoice(@PathVariable("orderId") UUID orderId) {
        return repository.findById(orderId)
                .<ResponseEntity<JsonNode>>map(inv -> ResponseEntity.ok(objectMapper.valueToTree(inv)))
                .orElseGet(() -> ResponseEntity.status(404).body(objectMapper.createObjectNode().put("error", "Not found")));
    }
}
