package com.foodplatform.notification.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID paymentId,
        UUID orderId,
        UUID customerId,
        BigDecimal amount,
        String currency,
        String reason
) {}
