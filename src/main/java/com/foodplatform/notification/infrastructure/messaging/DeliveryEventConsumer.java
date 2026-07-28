package com.foodplatform.notification.infrastructure.messaging;

import com.foodplatform.notification.application.NotificationApplicationService;
import com.foodplatform.notification.infrastructure.messaging.event.DeliveryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEventConsumer.class);

    private final NotificationApplicationService notificationApplicationService;

    public DeliveryEventConsumer(NotificationApplicationService notificationApplicationService) {
        this.notificationApplicationService = notificationApplicationService;
    }

    @KafkaListener(
            topics = "${notification.kafka.topics.delivery-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "deliveryEventKafkaListenerContainerFactory"
    )
    public void consume(DeliveryEvent event) {
        log.debug("Received delivery event type={} orderId={}", event.eventType(), event.orderId());
        notificationApplicationService.handleDeliveryEvent(event);
    }
}
