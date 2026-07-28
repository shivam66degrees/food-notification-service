package com.foodplatform.notification.presentation.controller;

import com.foodplatform.notification.application.NotificationInboxService;
import com.foodplatform.notification.application.NotificationNotFoundException;
import com.foodplatform.notification.infrastructure.config.SecurityConfig;
import com.foodplatform.notification.infrastructure.security.GatewayAuthenticationFilter;
import com.foodplatform.notification.presentation.advice.NotificationExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationInboxController.class)
@Import({SecurityConfig.class, GatewayAuthenticationFilter.class, NotificationExceptionHandler.class})
class NotificationInboxControllerSecurityTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("e2e00000-0000-4000-8000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationInboxService notificationInboxService;

    @Test
    void listInbox_withoutHeaders_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/notifications/inbox"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listInbox_withCustomerHeaders_returnsOk() throws Exception {
        UUID notificationId = UUID.randomUUID();
        var item = new NotificationInboxService.NotificationItem(
                notificationId,
                UUID.randomUUID(),
                "PaymentConfirmed",
                "Payment confirmed",
                "Your payment was confirmed.",
                null,
                Instant.parse("2026-07-28T00:00:00Z")
        );
        var page = new NotificationInboxService.InboxPage(List.of(item), 0, 20, 1);

        when(notificationInboxService.listInbox(eq(CUSTOMER_ID), eq(0), eq(20), eq(false))).thenReturn(page);

        mockMvc.perform(get("/notifications/inbox")
                        .header(GatewayAuthenticationFilter.USER_ID_HEADER, CUSTOMER_ID)
                        .header(GatewayAuthenticationFilter.USER_ROLES_HEADER, "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(notificationId.toString()))
                .andExpect(jsonPath("$.items[0].title").value("Payment confirmed"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void markAsRead_notFound_returns404() throws Exception {
        UUID notificationId = UUID.randomUUID();

        when(notificationInboxService.markAsRead(CUSTOMER_ID, notificationId))
                .thenThrow(new NotificationNotFoundException());

        mockMvc.perform(patch("/notifications/{id}/read", notificationId)
                        .header(GatewayAuthenticationFilter.USER_ID_HEADER, CUSTOMER_ID)
                        .header(GatewayAuthenticationFilter.USER_ROLES_HEADER, "CUSTOMER"))
                .andExpect(status().isNotFound());
    }
}
