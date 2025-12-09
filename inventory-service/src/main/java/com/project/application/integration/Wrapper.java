package com.project.application.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class Wrapper {
    private String eventType;
    private Map<String, Object> data;
}
