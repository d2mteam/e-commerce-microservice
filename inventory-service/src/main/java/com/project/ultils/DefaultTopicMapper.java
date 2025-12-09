package com.project.ultils;

import com.project.application.integration.impl.ProductReleaseReply;
import com.project.application.integration.impl.ProductReleaseRequest;
import com.project.application.integration.impl.ProductReserveReply;
import com.project.application.integration.impl.ProductReserveRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DefaultTopicMapper implements TopicMapper {

    private final Map<String, List<String>> registry = Map.of(
            ProductReserveRequest.class.getSimpleName(), List.of("inventory-topic"),
            ProductReleaseRequest.class.getSimpleName(), List.of("inventory-topic"),
            ProductReserveReply.class.getSimpleName(), List.of("order-topic"),
            ProductReleaseReply.class.getSimpleName(), List.of("order-topic")
    );

    @Override
    public List<String> getTopicsFromEventType(String type) {
        return registry.getOrDefault(type, List.of("default.topic"));
    }
}
