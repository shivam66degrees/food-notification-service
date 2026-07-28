package com.foodplatform.notification.presentation.controller;

import com.foodplatform.notification.application.NotificationInboxService;
import com.foodplatform.notification.infrastructure.security.GatewayUserContext;
import com.foodplatform.notification.presentation.dto.InboxPageResponse;
import com.foodplatform.notification.presentation.dto.NotificationItemResponse;
import com.foodplatform.notification.presentation.mapper.NotificationResponseMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationInboxController {

    private final NotificationInboxService notificationInboxService;

    public NotificationInboxController(NotificationInboxService notificationInboxService) {
        this.notificationInboxService = notificationInboxService;
    }

    @GetMapping("/inbox")
    public ResponseEntity<InboxPageResponse> listInbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        UUID userId = GatewayUserContext.requireUserId();
        var inbox = notificationInboxService.listInbox(userId, page, size, unreadOnly);
        return ResponseEntity.ok(NotificationResponseMapper.toInboxPageResponse(inbox));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationItemResponse> markAsRead(@PathVariable UUID notificationId) {
        UUID userId = GatewayUserContext.requireUserId();
        var item = notificationInboxService.markAsRead(userId, notificationId);
        return ResponseEntity.ok(NotificationResponseMapper.toItemResponse(item));
    }
}
