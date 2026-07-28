package com.foodplatform.notification.integration;

import com.foodplatform.notification.application.NotificationApplicationService;
import com.foodplatform.notification.application.NotificationStreamService;
import com.foodplatform.notification.infrastructure.messaging.event.PaymentEvent;
import com.foodplatform.notification.infrastructure.messaging.event.PaymentEventTypes;
import com.foodplatform.notification.infrastructure.persistence.SpringDataNotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"payment.events", "order.events", "delivery.events"})
class NotificationPersistenceIntegrationTest extends PostgresIntegrationSupport {

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registerPostgresProperties(registry);
        registerEmbeddedKafkaProperties(registry);
        registry.add("notification.kafka.topics.payment-events", () -> "payment.events");
        registry.add("notification.kafka.topics.order-events", () -> "order.events");
        registry.add("notification.kafka.topics.delivery-events", () -> "delivery.events");
    }

    @MockBean
    private NotificationStreamService notificationStreamService;

    @Autowired
    private NotificationApplicationService notificationApplicationService;

    @Autowired
    private SpringDataNotificationRepository notificationRepository;

    @Test
    void handlePaymentConfirmedEvent_persistsNotificationAndRecipientMapping() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        PaymentEvent event = new PaymentEvent(
                eventId,
                PaymentEventTypes.PAYMENT_CONFIRMED,
                Instant.now(),
                paymentId,
                orderId,
                customerId,
                new BigDecimal("12.99"),
                "INR",
                null
        );

        notificationApplicationService.handlePaymentEvent(event);

        assertTrue(notificationRepository.existsBySourceEventId(eventId));

        var inbox = notificationRepository.findByRecipientUserId(customerId, PageRequest.of(0, 10));
        assertEquals(1, inbox.getTotalElements());
        assertEquals("Payment confirmed", inbox.getContent().getFirst().getTitle());
        assertEquals(orderId, inbox.getContent().getFirst().getOrderId());
    }
}
