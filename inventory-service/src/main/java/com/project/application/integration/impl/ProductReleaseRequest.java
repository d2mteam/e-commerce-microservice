package com.project.application.integration.impl;

import com.project.application.integration.IntegrationEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductReleaseRequest extends IntegrationEvent {
    private UUID orderId;
    private UUID productId;
}
