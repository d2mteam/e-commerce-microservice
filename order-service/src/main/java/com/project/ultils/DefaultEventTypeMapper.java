package com.project.ultils;

import com.project.domain.order.event.*;
import com.project.event_sourcing_core.domain.event.Event;
import com.project.event_sourcing_core.domain.event.EventTypeMapper;
import org.springframework.stereotype.Component;

@Component
public class DefaultEventTypeMapper implements EventTypeMapper {
    @Override
    public Class<? extends Event> getClassByEventType(String eventType) {
        return switch (eventType) {
            case "OrderCreatedEvent" -> OrderCreatedEvent.class;
            case "OrderCancelledEvent" -> OrderCancelledEvent.class;


            case "ShippingStartedEvent" -> ShippingStartedEvent.class;
            case "PaymentReceivedEvent" -> PaymentReceivedEvent.class;
            case "InventoryOutOfStockEvent" -> InventoryOutOfStockEvent.class;
            case "InventoryConfirmedEvent" -> InventoryConfirmedEvent.class;
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
