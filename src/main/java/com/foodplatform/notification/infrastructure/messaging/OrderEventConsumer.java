package com.foodplatform.notification.infrastructure.messaging;

import com.foodplatform.notification.application.NotificationApplicationService;
import com.foodplatform.notification.infrastructure.messaging.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final NotificationApplicationService notificationApplicationService;

    public OrderEventConsumer(NotificationApplicationService notificationApplicationService) {
        this.notificationApplicationService = notificationApplicationService;
    }

    @KafkaListener(
            topics = "${notification.kafka.topics.order-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderEventKafkaListenerContainerFactory"
    )
    public void consume(OrderEvent event) {
        log.debug("Received order event type={} orderId={}", event.eventType(), event.orderId());
        notificationApplicationService.handleOrderEvent(event);
    }
}
