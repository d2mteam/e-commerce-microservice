package com.project.ultils;

import com.project.application.integration.IntegrationEvent;
import com.project.application.integration.impl.ProductReleaseReply;
import com.project.application.integration.impl.ProductReserveReply;
import com.project.application.integration.mapper.DomainToIntegrationEventMapper;
import com.project.domain.inventory.event.StockReleasedEvent;
import com.project.domain.inventory.event.StockReservedEvent;
import com.project.event_sourcing_core.domain.event.Event;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InventoryEventMapper implements DomainToIntegrationEventMapper<Event> {

    @Override
    public boolean supports(Class<?> eventClass) {
        return eventClass == StockReservedEvent.class
                || eventClass == StockReleasedEvent.class;
    }

    @Override
    public List<IntegrationEvent> map(Event event) {
        List<IntegrationEvent> result = new ArrayList<>();

        if (event instanceof StockReservedEvent reserved) {
            result.add(ProductReserveReply.builder()
                    .orderId(reserved.getOrderId())
                    .productId(event.getAggregateId())
                    .result(ProductReserveReply.Result.SUCCESS)
                    .correlationId(reserved.getOrderId().toString())
                    .build());
        }

        if (event instanceof StockReleasedEvent released) {
            result.add(ProductReleaseReply.builder()
                    .orderId(released.getOrderId())
                    .productId(event.getAggregateId())
                    .result(ProductReleaseReply.Result.SUCCESS)
                    .correlationId(released.getOrderId().toString())
                    .build());
        }

        return result;
    }
}
