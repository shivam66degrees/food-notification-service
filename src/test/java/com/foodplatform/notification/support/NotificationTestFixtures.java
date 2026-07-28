package com.foodplatform.notification.support;

import com.foodplatform.notification.application.NotificationInboxService;
import com.foodplatform.notification.infrastructure.persistence.NotificationJpaEntity;

import java.time.Instant;
import java.util.UUID;

public final class NotificationTestFixtures {

    public static final UUID CUSTOMER_ID = UUID.fromString("e2e00000-0000-4000-8000-000000000001");
    public static final UUID ORDER_ID = UUID.fromString("44444444-4444-4444-8444-444444444444");
    public static final UUID NOTIFICATION_ID = UUID.fromString("11111111-1111-4111-8111-111111111101");
    public static final Instant FIXED_TIME = Instant.parse("2026-07-28T00:00:00Z");

    private NotificationTestFixtures() {}

    public static NotificationJpaEntity sampleEntity() {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(NOTIFICATION_ID);
        entity.setRecipientUserId(CUSTOMER_ID);
        entity.setOrderId(ORDER_ID);
        entity.setEventType("PaymentConfirmed");
        entity.setTitle("Payment confirmed");
        entity.setBody("Your payment was confirmed.");
        entity.setCreatedAt(FIXED_TIME);
        return entity;
    }

    public static NotificationInboxService.NotificationItem sampleItem() {
        return new NotificationInboxService.NotificationItem(
                NOTIFICATION_ID,
                ORDER_ID,
                "PaymentConfirmed",
                "Payment confirmed",
                "Your payment was confirmed.",
                null,
                FIXED_TIME
        );
    }

    public static NotificationInboxService.InboxPage samplePage() {
        return new NotificationInboxService.InboxPage(
                java.util.List.of(sampleItem()),
                0,
                20,
                1
        );
    }
}
