package com.project.orderservice.ultils;

import com.project.orderservice.domain.event.*;
import com.project.orderservice.event_sourcing_core.domain.event.Event;
import com.project.orderservice.event_sourcing_core.domain.event.EventTypeMapper;
import org.springframework.stereotype.Component;

@Component
public class DefaultEventTypeMapper implements EventTypeMapper {
    @Override
    public Class<? extends Event> getClassByEventType(String eventType) {
        return switch (eventType) {
            case "OrderCreatedEvent" -> OrderCreatedEvent.class;
            case "OrderCancelledEvent" -> OrderCancelledEvent.class;
            case "OrderConfirmedEvent" -> OrderConfirmedEvent.class;
            case "ShippingStartedEvent" -> ShippingStartedEvent.class;
            case "PaymentReceivedEvent" -> PaymentReceivedEvent.class;
            case "InventoryOutOfStockEvent" -> InventoryOutOfStockEvent.class;
            case "InventoryConfirmedEvent" -> InventoryConfirmedEvent.class;
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
