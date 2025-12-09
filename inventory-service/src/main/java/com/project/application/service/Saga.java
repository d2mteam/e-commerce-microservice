package com.project.application.service;

import com.project.application.integration.impl.ProductReleaseReply;
import com.project.application.integration.impl.ProductReleaseRequest;
import com.project.application.integration.impl.ProductReserveReply;
import com.project.application.integration.impl.ProductReserveRequest;
import com.project.domain.inventory.command.ReleaseStockCommand;
import com.project.domain.inventory.command.ReserveStockCommand;
import com.project.event_sourcing_core.domain.command.Command;
import com.project.event_sourcing_core.service.CommandProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Saga {
    private final CommandProcessor commandProcessor;
    private final IntegrationEventOutboxWriter outboxWriter;

    @EventListener
    public void handle(ProductReserveRequest integrationEvent) {
        Command command = ReserveStockCommand.builder()
                .aggregateId(integrationEvent.getProductId())
                .orderId(integrationEvent.getOrderId())
                .quantity(integrationEvent.getQuantity())
                .build();

        try {
            commandProcessor.process(command);
        } catch (Exception ex) {
            log.error("Failed to reserve product {} for order {}: {}",
                    integrationEvent.getProductId(), integrationEvent.getOrderId(), ex.getMessage(), ex);
            publishReserveFailure(integrationEvent, ex.getMessage());
        }
    }

    @EventListener
    public void handle(ProductReleaseRequest integrationEvent) {
        Command command = ReleaseStockCommand.builder()
                .aggregateId(integrationEvent.getProductId())
                .orderId(integrationEvent.getOrderId())
                .build();

        try {
            commandProcessor.process(command);
        } catch (Exception ex) {
            log.error("Failed to release product {} for order {}: {}",
                    integrationEvent.getProductId(), integrationEvent.getOrderId(), ex.getMessage(), ex);
            publishReleaseFailure(integrationEvent, ex.getMessage());
        }
    }

    private void publishReserveFailure(ProductReserveRequest request, String reason) {
        outboxWriter.persist(request.getProductId(),
                ProductReserveReply.builder()
                        .orderId(request.getOrderId())
                        .productId(request.getProductId())
                        .result(ProductReserveReply.Result.FAILURE)
                        .reason(reason)
                        .correlationId(request.getCorrelationId())
                        .build());
    }

    private void publishReleaseFailure(ProductReleaseRequest request, String reason) {
        outboxWriter.persist(request.getProductId(),
                ProductReleaseReply.builder()
                        .orderId(request.getOrderId())
                        .productId(request.getProductId())
                        .result(ProductReleaseReply.Result.FAILURE)
                        .reason(reason)
                        .correlationId(request.getCorrelationId())
                        .build());
    }
}
