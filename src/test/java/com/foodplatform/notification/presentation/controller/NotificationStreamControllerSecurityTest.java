package com.foodplatform.notification.presentation.controller;

import com.foodplatform.notification.application.NotificationStreamService;
import com.foodplatform.notification.infrastructure.config.SecurityConfig;
import com.foodplatform.notification.infrastructure.security.GatewayAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationStreamController.class)
@Import({SecurityConfig.class, GatewayAuthenticationFilter.class})
class NotificationStreamControllerSecurityTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("e2e00000-0000-4000-8000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationStreamService notificationStreamService;

    @Test
    void stream_withoutHeaders_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/notifications/stream"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void stream_withCustomerHeaders_returnsOk() throws Exception {
        when(notificationStreamService.subscribe(eq(CUSTOMER_ID))).thenReturn(new SseEmitter(1000L));

        mockMvc.perform(get("/notifications/stream")
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .header(GatewayAuthenticationFilter.USER_ID_HEADER, CUSTOMER_ID)
                        .header(GatewayAuthenticationFilter.USER_ROLES_HEADER, "CUSTOMER"))
                .andExpect(status().isOk());
    }
}
