package com.foodplatform.notification.infrastructure.messaging;

import com.foodplatform.notification.application.NotificationApplicationService;
import com.foodplatform.notification.infrastructure.messaging.event.PaymentEvent;
import com.foodplatform.notification.infrastructure.messaging.event.PaymentEventTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock
    private NotificationApplicationService notificationApplicationService;

    @InjectMocks
    private PaymentEventConsumer paymentEventConsumer;

    @Test
    void consume_delegatesToApplicationService() {
        PaymentEvent event = new PaymentEvent(
                UUID.randomUUID(),
                PaymentEventTypes.PAYMENT_CONFIRMED,
                Instant.now(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("12.99"),
                "INR",
                null
        );

        paymentEventConsumer.consume(event);

        verify(notificationApplicationService).handlePaymentEvent(event);
    }
}
