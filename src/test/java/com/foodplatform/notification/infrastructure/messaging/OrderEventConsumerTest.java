package com.foodplatform.notification.infrastructure.messaging;

import com.foodplatform.notification.application.NotificationApplicationService;
import com.foodplatform.notification.infrastructure.messaging.event.OrderEvent;
import com.foodplatform.notification.infrastructure.messaging.event.OrderEventTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private NotificationApplicationService notificationApplicationService;

    @InjectMocks
    private OrderEventConsumer orderEventConsumer;

    @Test
    void consume_delegatesToApplicationService() {
        OrderEvent event = new OrderEvent(
                UUID.randomUUID(),
                OrderEventTypes.ORDER_READY_FOR_DELIVERY,
                Instant.now(),
                UUID.randomUUID()
        );

        orderEventConsumer.consume(event);

        verify(notificationApplicationService).handleOrderEvent(event);
    }
}
