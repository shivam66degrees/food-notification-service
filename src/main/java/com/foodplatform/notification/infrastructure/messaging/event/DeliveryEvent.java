package com.foodplatform.notification.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeliveryEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID orderId
) {}
