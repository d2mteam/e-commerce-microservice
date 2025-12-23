package com.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentExpiryScheduler {

    private final PaymentService paymentService;

    @Scheduled(fixedDelay = 5000)
    public void expirePending() {
        paymentService.expirePending();
    }
}
