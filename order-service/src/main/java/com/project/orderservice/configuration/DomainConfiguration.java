package com.project.orderservice.configuration;

import com.project.orderservice.domain.Event;
import com.project.orderservice.domain.command.CreateOrderCommand;
import com.project.orderservice.domain.event.OrderCancelledEvent;
import com.project.orderservice.domain.event.OrderCompletedEvent;
import com.project.orderservice.domain.event.OrderCreatedEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DomainConfiguration {
    @Bean
    public Map<String, Class<? extends Event>> eventTypeMap() {
        Map<String, Class<? extends Event>> eventTypeMap = new HashMap<>();
        eventTypeMap.put(OrderCancelledEvent.class.getSimpleName(), OrderCancelledEvent.class);
        eventTypeMap.put(OrderCreatedEvent.class.getSimpleName(), OrderCreatedEvent.class);
        eventTypeMap.put(OrderCompletedEvent.class.getSimpleName(), OrderCompletedEvent.class);
        return eventTypeMap;
    }
}
