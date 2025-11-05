package com.project.orderservice.ultils;

import com.project.orderservice.domain.event.OrderCancelledEvent;
import com.project.orderservice.domain.event.OrderCreatedEvent;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class EventRoutingMapper {
    private final Map<String, List<String>> routing;

    public EventRoutingMapper(Map<String, List<String>> routing) {
        this.routing = routing;
    }

    public static EventRoutingMapper demo = new EventRoutingMapper(
            Map.of(OrderCreatedEvent.class.getSimpleName(), List.of("http://localhost:1000/api/integration/events"),
                    OrderCancelledEvent.class.getSimpleName(), List.of("http://localhost:1000/api/integration/events")));
}
