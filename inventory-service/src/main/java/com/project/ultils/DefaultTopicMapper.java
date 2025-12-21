package com.project.ultils;

import com.project.integration.message.ProductReleaseReply;
import com.project.integration.message.ProductReserveReply;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DefaultTopicMapper implements TopicMapper {

    private final Map<String, List<String>> registry = Map.of(
            ProductReserveReply.class.getSimpleName(), List.of("order-topic"),
            ProductReleaseReply.class.getSimpleName(), List.of("order-topic")
    );

    @Override
    public List<String> getTopicsFromEventType(String type) {
        return registry.getOrDefault(type, List.of("default.topic"));
    }
}
