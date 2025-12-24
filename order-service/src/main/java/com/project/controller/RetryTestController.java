package com.project.controller;

import com.project.infrastructure.external.PaymentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RetryTestController {

    private final PaymentClient paymentClient;

    @GetMapping("/api/test-retry")
    public String testRetry(@RequestParam(defaultValue = "test-order-id") String orderId) {
        // Gọi hàm có @Retryable
        boolean result = paymentClient.processPayment(orderId, 100.0);
        
        if (result) {
            return "Payment Successful (May have retried)";
        } else {
            return "Payment Failed (Fallback executed)";
        }
    }
}
