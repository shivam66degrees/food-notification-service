package com.foodplatform.notification.presentation.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record NotificationItemResponse(
        UUID id,
        UUID orderId,
        String eventType,
        String title,
        String body,
        Instant readAt,
        Instant createdAt
) {}
