package com.project.orderservice.ultils;

import com.project.orderservice.domain.event.OrderCancelledEvent;
import com.project.orderservice.domain.event.OrderConfirmedEvent;
import com.project.orderservice.domain.event.OrderCreatedEvent;
import com.project.orderservice.event_sourcing_core.domain.event.Event;
import com.project.orderservice.event_sourcing_core.domain.event.EventTypeMapper;
import org.springframework.stereotype.Component;

@Component
public class DefaultEventTypeMapper implements EventTypeMapper {
    @Override
    public Class<? extends Event> getClassByEventType(String eventType) {
        if (eventType.equals(OrderCreatedEvent.class.getSimpleName())) {
            return OrderCreatedEvent.class;
        }

        if  (eventType.equals(OrderCancelledEvent.class.getSimpleName())) {
            return OrderCancelledEvent.class;
        }

        if (eventType.equals(OrderConfirmedEvent.class.getSimpleName())) {
            return OrderConfirmedEvent.class;
        }

        throw new IllegalArgumentException("Unsupported event type: " + eventType);
    }
}
