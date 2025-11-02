package com.project.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private static final String REALM_ACCESS = "realm_access";
    private static final String RESOURCE_ACCESS = "resource_access";
    private static final String ROLES = "roles";
    private static final String ROLE_PREFIX = "ROLE_";
    private final String clientId;

    public KeycloakRealmRoleConverter() {
        // If you created a specific client for your API in Keycloak, put its clientId here (or make it configurable)
        this.clientId = "spring-gateway";
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<String> roles = new HashSet<>();

        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS);
        if (realmAccess != null) {
            Object realmRoles = realmAccess.get(ROLES);
            if (realmRoles instanceof Collection<?> rr) {
                rr.forEach(r -> roles.add(String.valueOf(r)));
            }
        }

        Map<String, Object> resourceAccess = jwt.getClaim(RESOURCE_ACCESS);
        if (resourceAccess != null) {
            Object clientObj = resourceAccess.get(clientId);
            if (clientObj instanceof Map<?, ?> map) {
                Object clientRoles = map.get(ROLES);
                if (clientRoles instanceof Collection<?> cr) {
                    cr.forEach(r -> roles.add(String.valueOf(r)));
                }
            }
        }

        return roles.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(r -> new SimpleGrantedAuthority(ROLE_PREFIX + r))
                .collect(Collectors.toSet());
    }
}
