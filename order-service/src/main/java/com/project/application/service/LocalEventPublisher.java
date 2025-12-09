package com.project.application.service;

import com.project.application.integration.IntegrationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(IntegrationEvent integrationEvent) {
        applicationEventPublisher.publishEvent(integrationEvent);
    }
}
