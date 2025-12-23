package com.project.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
public class UserContextFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(authentication -> authentication instanceof JwtAuthenticationToken)
            .cast(JwtAuthenticationToken.class)
            .flatMap(jwtToken -> {
                String userId = jwtToken.getToken().getClaimAsString("sub");
                String username = jwtToken.getToken().getClaimAsString("preferred_username");
                String email = jwtToken.getToken().getClaimAsString("email");

                String roles = jwtToken.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));

                ServerHttpRequest request = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Name", username)
                        .header("X-User-Email", email)
                        .header("X-User-Roles", roles)
                        .build();

                return chain.filter(exchange.mutate().request(request).build());
            })
            .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
