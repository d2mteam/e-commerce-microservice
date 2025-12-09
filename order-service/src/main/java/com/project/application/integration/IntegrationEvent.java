package com.project.application.integration;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class IntegrationEvent {
    protected String correlationId;

    @Builder.Default
    private final String eventType = null;

    protected OffsetDateTime createdAt;


    public String getEventType() {
        return this.getClass().getSimpleName();
    }
}
