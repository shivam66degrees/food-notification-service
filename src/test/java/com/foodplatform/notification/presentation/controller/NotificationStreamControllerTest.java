package com.foodplatform.notification.presentation.controller;

import com.foodplatform.notification.application.NotificationStreamService;
import com.foodplatform.notification.infrastructure.security.GatewayAuthenticationFilter;
import com.foodplatform.notification.infrastructure.security.GatewayUserContext;
import com.foodplatform.notification.support.NotificationTestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationStreamController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationStreamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationStreamService notificationStreamService;

    @MockBean
    private GatewayAuthenticationFilter gatewayAuthenticationFilter;

    @BeforeEach
    void setUserContext() {
        GatewayUserContext.setUserId(NotificationTestFixtures.CUSTOMER_ID);
    }

    @AfterEach
    void clearContext() {
        GatewayUserContext.clear();
    }

    @Test
    void stream_returnsOk() throws Exception {
        when(notificationStreamService.subscribe(NotificationTestFixtures.CUSTOMER_ID))
                .thenReturn(new SseEmitter(60_000L));

        mockMvc.perform(get("/notifications/stream"))
                .andExpect(status().isOk());
    }
}
