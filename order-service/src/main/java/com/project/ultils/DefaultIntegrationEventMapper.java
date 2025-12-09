package com.project.ultils;

import com.project.application.integration.*;
import com.project.application.integration.impl.ProductReleaseRequest;
import com.project.application.integration.impl.ProductReleaseReply;
import com.project.application.integration.impl.ProductReserveReply;
import com.project.application.integration.impl.ProductReserveRequest;
import com.project.application.integration.mapper.IntegrationEventMapper;
import org.springframework.stereotype.Component;

@Component
public class DefaultIntegrationEventMapper implements IntegrationEventMapper {
    @Override
    public Class<? extends IntegrationEvent> getClassByIntegrationEventTypeMapper(String type) {
        if (ProductReserveReply.class.getSimpleName().equals(type)) {
            return ProductReserveReply.class;
        }

        if (ProductReserveRequest.class.getSimpleName().equals(type)) {
            return ProductReserveRequest.class;
        }

        if (ProductReleaseRequest.class.getSimpleName().equals(type)) {
            return ProductReleaseRequest.class;
        }

        if (ProductReleaseReply.class.getSimpleName().equals(type)) {
            return ProductReleaseReply.class;
        }

        return null;
    }
}
