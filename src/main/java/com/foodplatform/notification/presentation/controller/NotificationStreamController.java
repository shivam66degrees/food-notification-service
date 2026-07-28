package com.foodplatform.notification.presentation.controller;

import com.foodplatform.notification.application.NotificationStreamService;
import com.foodplatform.notification.infrastructure.security.GatewayUserContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationStreamController {

    private final NotificationStreamService notificationStreamService;

    public NotificationStreamController(NotificationStreamService notificationStreamService) {
        this.notificationStreamService = notificationStreamService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        UUID userId = GatewayUserContext.requireUserId();
        return notificationStreamService.subscribe(userId);
    }
}
