package com.project.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "rate-limit")
@Data
public class RateLimitConfig {

    private boolean enabled = true;

    // Authenticated users (per User ID)
    private int userCapacity = 100;
    private int userRefillTokens = 100;

    // Anonymous users (per IP - higher limit for NAT)
    private int ipCapacity = 500;
    private int ipRefillTokens = 500;

    // Common
    private Duration refillDuration = Duration.ofMinutes(1);
}
