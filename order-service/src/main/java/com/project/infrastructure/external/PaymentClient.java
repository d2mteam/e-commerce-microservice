package com.project.infrastructure.external;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

@Service
public class PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);

    /**
     * Gọi API thanh toán với Circuit Breaker + Retry.
     * Circuit Breaker sẽ mở khi có quá nhiều lỗi liên tiếp.
     * Retry sẽ thử lại khi có lỗi tạm thời.
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Retryable(
            value = { ResourceAccessException.class, RestClientException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public boolean processPayment(String orderId, double amount) {
        log.info("===> Calling Payment Service for Order: {}, Amount: {}", orderId, amount);

        // TODO: Replace with actual HTTP call to payment service
        // RestTemplate or WebClient call here

        // Simulated success for now
        log.info("Payment Successful for Order: {}", orderId);
        return true;
    }

    /**
     * Fallback khi Circuit Breaker OPEN hoặc tất cả retries fail.
     * Đánh dấu đơn hàng chờ thanh toán sau.
     */
    public boolean paymentFallback(String orderId, double amount, Throwable ex) {
        log.error("Payment Service unavailable. Circuit Breaker activated for Order: {}. Error: {}",
                orderId, ex.getMessage());

        // Logic fallback:
        // 1. Đánh dấu đơn hàng "PENDING_PAYMENT"
        // 2. Queue để retry sau
        // 3. Notify admin

        return false;
    }
}
