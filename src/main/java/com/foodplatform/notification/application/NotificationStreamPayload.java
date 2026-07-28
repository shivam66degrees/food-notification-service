package com.foodplatform.notification.application;

import java.time.Instant;
import java.util.UUID;

public record NotificationStreamPayload(
        UUID id,
        UUID orderId,
        String eventType,
        String title,
        String body,
        Instant createdAt
) {}
