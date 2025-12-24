package com.project.productservice.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class SecurityAspect {

    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String rolesHeader = request.getHeader("X-User-Roles");

        if (rolesHeader == null || rolesHeader.isEmpty()) {
            throw new RuntimeException("Access Denied: No roles found");
        }

        List<String> userRoles = Arrays.asList(rolesHeader.split(","));
        for (String requiredRole : requireRole.value()) {
            // Check for ROLE_ prefix as converted by Gateway
            if (userRoles.contains("ROLE_" + requiredRole) || userRoles.contains(requiredRole)) {
                return joinPoint.proceed();
            }
        }

        throw new RuntimeException("Access Denied: Insufficient privileges");
    }
}
