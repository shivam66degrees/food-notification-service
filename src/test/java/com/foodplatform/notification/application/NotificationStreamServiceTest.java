package com.foodplatform.notification.application;

import com.foodplatform.notification.infrastructure.persistence.NotificationJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationStreamServiceTest {

    @Test
    void publish_withoutSubscribers_doesNotThrow() {
        NotificationStreamService service = new NotificationStreamService(60_000L);
        UUID recipientId = UUID.randomUUID();
        NotificationStreamPayload payload = new NotificationStreamPayload(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "PaymentConfirmed",
                "Payment confirmed",
                "Body",
                Instant.now()
        );

        service.publish(recipientId, payload);
    }

    @Test
    void fromEntity_mapsNotificationFields() {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-07-28T00:00:00Z");
        entity.setId(id);
        entity.setOrderId(orderId);
        entity.setEventType("DeliveryDelivered");
        entity.setTitle("Delivered");
        entity.setBody("Enjoy!");
        entity.setCreatedAt(createdAt);

        NotificationStreamPayload payload = NotificationStreamService.fromEntity(entity);

        assertThat(payload.id()).isEqualTo(id);
        assertThat(payload.orderId()).isEqualTo(orderId);
        assertThat(payload.eventType()).isEqualTo("DeliveryDelivered");
        assertThat(payload.title()).isEqualTo("Delivered");
        assertThat(payload.body()).isEqualTo("Enjoy!");
        assertThat(payload.createdAt()).isEqualTo(createdAt);
    }
}
