package org.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/callme/**").authenticated()
                        .anyRequest().authenticated())
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract default authorities
            JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
            Collection<GrantedAuthority> defaultAuthorities = defaultConverter.convert(jwt);

            // Extract realm_access roles
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            Collection<GrantedAuthority> realmRoles = List.of();
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                realmRoles = ((List<String>) realmAccess.get("roles")).stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
            }

            // Extract resource_access roles
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            Collection<GrantedAuthority> resourceRoles = List.of();
            if (resourceAccess != null) {
                resourceRoles = resourceAccess.values().stream()
                        .filter(resource -> resource instanceof Map)
                        .flatMap(resource -> {
                            Object roles = ((Map<String, Object>) resource).get("roles");
                            if (roles instanceof List) {
                                return ((List<String>) roles).stream();
                            }
                            return Stream.empty();
                        })
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
            }

            // Combine all authorities
            return Stream.of(defaultAuthorities, realmRoles, resourceRoles)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        });
        return jwtAuthenticationConverter;
    }
}
