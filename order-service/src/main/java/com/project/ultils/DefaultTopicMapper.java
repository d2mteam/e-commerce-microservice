package com.project.ultils;

import com.project.integration.message.ProductReleaseRequest;
import com.project.integration.message.ProductReleaseReply;
import com.project.integration.message.ProductReserveReply;
import com.project.integration.message.ProductReserveRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DefaultTopicMapper implements TopicMapper {

    private final Map<String, List<String>> registry = Map.of(
            ProductReserveRequest.class.getSimpleName(), List.of("inventory-service"),
            ProductReleaseRequest.class.getSimpleName(), List.of("inventory-service"),
            ProductReserveReply.class.getSimpleName(), List.of("order-service"),
            ProductReleaseReply.class.getSimpleName(), List.of("order-service")
    );

    @Override
    public List<String> getTopicsFromEventType(String type) {
        return registry.getOrDefault(type, List.of("default.topic"));
    }
}
