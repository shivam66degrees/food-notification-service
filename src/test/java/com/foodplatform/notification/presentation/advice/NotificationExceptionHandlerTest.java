package com.foodplatform.notification.presentation.advice;

import com.foodplatform.notification.application.NotificationNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationExceptionHandlerTest {

    private final NotificationExceptionHandler handler = new NotificationExceptionHandler();

    @Test
    void handleNotFound_returnsNotFoundWithMessage() {
        var response = handler.handleNotFound(new NotificationNotFoundException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("message", "Notification not found");
    }
}
