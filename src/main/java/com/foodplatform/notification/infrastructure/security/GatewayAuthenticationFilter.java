package com.foodplatform.notification.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                UUID userId = UUID.fromString(userIdHeader.trim());
                Set<String> roles = parseRoles(request.getHeader(USER_ROLES_HEADER));
                var authenticatedUser = new GatewayAuthenticatedUser(userId, roles);
                var authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toSet());
                var authentication = new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        authorities
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                GatewayUserContext.setUserId(userId);
            } catch (IllegalArgumentException ex) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
            GatewayUserContext.clear();
        }
    }

    static Set<String> parseRoles(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return Set.of();
        }
        return Stream.of(headerValue.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }
}
