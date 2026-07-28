package com.foodplatform.notification.presentation.controller;

import com.foodplatform.notification.application.NotificationInboxService;
import com.foodplatform.notification.infrastructure.security.GatewayAuthenticationFilter;
import com.foodplatform.notification.infrastructure.security.GatewayUserContext;
import com.foodplatform.notification.presentation.advice.NotificationExceptionHandler;
import com.foodplatform.notification.support.NotificationTestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationInboxController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(NotificationExceptionHandler.class)
class NotificationInboxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationInboxService notificationInboxService;

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
    void listInbox_returnsOk() throws Exception {
        when(notificationInboxService.listInbox(NotificationTestFixtures.CUSTOMER_ID, 0, 20, false))
                .thenReturn(NotificationTestFixtures.samplePage());

        mockMvc.perform(get("/notifications/inbox"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(NotificationTestFixtures.NOTIFICATION_ID.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void markAsRead_returnsOk() throws Exception {
        when(notificationInboxService.markAsRead(
                eq(NotificationTestFixtures.CUSTOMER_ID),
                eq(NotificationTestFixtures.NOTIFICATION_ID)
        )).thenReturn(NotificationTestFixtures.sampleItem());

        mockMvc.perform(patch("/notifications/{notificationId}/read", NotificationTestFixtures.NOTIFICATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Payment confirmed"));
    }
}
