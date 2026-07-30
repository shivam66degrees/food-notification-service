package com.foodplatform.notification.presentation.advice;

import com.foodplatform.notification.application.NotificationNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationExceptionHandlerTest {

    private final NotificationExceptionHandler handler = new NotificationExceptionHandler();

    @Test
    void handleNotFound_returnsNotFoundWithBody() {
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/notifications/1/read");
        var response = handler.handleNotFound(new NotificationNotFoundException(), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("NOTIFICATION_NOT_FOUND", response.getBody().error());
        assertEquals("Notification not found", response.getBody().message());
        assertEquals("/notifications/1/read", response.getBody().path());
    }
}
