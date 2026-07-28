package com.foodplatform.notification.infrastructure.messaging;

import com.foodplatform.notification.application.NotificationApplicationService;
import com.foodplatform.notification.infrastructure.messaging.event.DeliveryEvent;
import com.foodplatform.notification.infrastructure.messaging.event.DeliveryEventTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeliveryEventConsumerTest {

    @Mock
    private NotificationApplicationService notificationApplicationService;

    @InjectMocks
    private DeliveryEventConsumer deliveryEventConsumer;

    @Test
    void consume_delegatesToApplicationService() {
        DeliveryEvent event = new DeliveryEvent(
                UUID.randomUUID(),
                DeliveryEventTypes.DELIVERY_DELIVERED,
                Instant.now(),
                UUID.randomUUID()
        );

        deliveryEventConsumer.consume(event);

        verify(notificationApplicationService).handleDeliveryEvent(event);
    }
}
