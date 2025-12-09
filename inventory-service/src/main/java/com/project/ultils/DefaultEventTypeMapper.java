package com.project.ultils;

import com.project.domain.inventory.event.*;
import com.project.domain.order.event.OrderCancelledEvent;
import com.project.domain.order.event.OrderCreatedEvent;
import com.project.event_sourcing_core.domain.event.Event;
import com.project.event_sourcing_core.domain.event.EventTypeMapper;
import org.springframework.stereotype.Component;

@Component
public class DefaultEventTypeMapper implements EventTypeMapper {
    @Override
    public Class<? extends Event> getClassByEventType(String eventType) {
        return switch (eventType) {
            case "InventoryCreatedEvent" -> InventoryCreatedEvent.class;
            case "ReservationCancelledEvent" -> ReservationCancelledEvent.class;
            case "StockAddedEvent" -> StockAddedEvent.class;
            case "StockReleasedEvent" -> StockReleasedEvent.class;
            case "StockReservedEvent" -> StockReservedEvent.class;


            case "OrderCreatedEvent" -> OrderCreatedEvent.class;
            case "OrderCancelledEvent" -> OrderCancelledEvent.class;

            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
