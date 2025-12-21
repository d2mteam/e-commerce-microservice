package com.project.integration;

import com.project.akka.serialization.CborSerializable;
import lombok.Builder;

import java.util.Map;

@Builder
public record IntegrationMessage(
        String type,
        Map<String, Object> payload
) implements CborSerializable {
}
