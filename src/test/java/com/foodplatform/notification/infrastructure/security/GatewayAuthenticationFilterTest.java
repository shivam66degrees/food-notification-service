package com.foodplatform.notification.infrastructure.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GatewayAuthenticationFilterTest {

    private final GatewayAuthenticationFilter filter = new GatewayAuthenticationFilter();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        GatewayUserContext.clear();
    }

    @Test
    void missingHeader_doesNotSetAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/notifications/stream");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void invalidUserId_returnsUnauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/notifications/stream");
        request.addHeader(GatewayAuthenticationFilter.USER_ID_HEADER, "not-a-uuid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void validHeader_setsSecurityContextAndUserContext() throws Exception {
        UUID userId = UUID.fromString("e2e00000-0000-4000-8000-000000000001");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/notifications/stream");
        request.addHeader(GatewayAuthenticationFilter.USER_ID_HEADER, userId.toString());
        request.addHeader(GatewayAuthenticationFilter.USER_ROLES_HEADER, "CUSTOMER");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertEquals(userId, GatewayUserContext.requireUserId());
        };

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
    }

    @Test
    void actuatorPath_skipsAuthenticationRequirement() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }
}
