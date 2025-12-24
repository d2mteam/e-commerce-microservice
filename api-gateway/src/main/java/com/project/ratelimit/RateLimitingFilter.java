package com.project.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private final RateLimitConfig config;

    // Separate buckets for users and IPs
    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(RateLimitConfig config) {
        this.config = config;
        log.info("RateLimitingFilter initialized - enabled: {}, user: {} req/min, ip: {} req/min",
                config.isEnabled(),
                config.getUserCapacity(),
                config.getIpCapacity());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!config.isEnabled()) {
            return chain.filter(exchange);
        }

        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .flatMap(jwtAuth -> {
                    // Authenticated user → rate limit by User ID
                    String userId = jwtAuth.getToken().getClaimAsString("sub");
                    return applyRateLimit(exchange, chain, "user:" + userId, true);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Anonymous → rate limit by IP
                    String clientIp = resolveClientIp(exchange.getRequest());
                    return applyRateLimit(exchange, chain, "ip:" + clientIp, false);
                }));
    }

    private Mono<Void> applyRateLimit(ServerWebExchange exchange, GatewayFilterChain chain,
                                       String key, boolean isAuthenticated) {
        Bucket bucket;
        if (isAuthenticated) {
            bucket = userBuckets.computeIfAbsent(key, k -> createUserBucket());
        } else {
            bucket = ipBuckets.computeIfAbsent(key, k -> createIpBucket());
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            exchange.getResponse().getHeaders()
                    .add("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return chain.filter(exchange);
        }

        log.warn("Rate limit exceeded for {}", key);
        return handleRateLimitExceeded(exchange, probe);
    }

    private Bucket createUserBucket() {
        Refill refill = Refill.intervally(config.getUserRefillTokens(), config.getRefillDuration());
        Bandwidth limit = Bandwidth.classic(config.getUserCapacity(), refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createIpBucket() {
        Refill refill = Refill.intervally(config.getIpRefillTokens(), config.getRefillDuration());
        Bandwidth limit = Bandwidth.classic(config.getIpCapacity(), refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientIp(ServerHttpRequest request) {
        // 1. X-Forwarded-For (reverse proxy)
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        // 2. X-Real-IP
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp.trim();
        }

        // 3. Remote address
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null) {
            return remoteAddress.getAddress().getHostAddress();
        }

        return "unknown";
    }

    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange, ConsumptionProbe probe) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("X-RateLimit-Remaining", "0");
        response.getHeaders().add("Retry-After",
                String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
        String body = String.format(
                "{\"status\":\"error\",\"message\":\"Rate limit exceeded\",\"retryAfter\":%d}",
                retryAfterSeconds);

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
