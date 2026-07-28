package com.foodplatform.notification.infrastructure.messaging;

import com.foodplatform.notification.application.NotificationApplicationService;
import com.foodplatform.notification.infrastructure.messaging.event.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final NotificationApplicationService notificationApplicationService;

    public PaymentEventConsumer(NotificationApplicationService notificationApplicationService) {
        this.notificationApplicationService = notificationApplicationService;
    }

    @KafkaListener(
            topics = "${notification.kafka.topics.payment-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "paymentEventKafkaListenerContainerFactory"
    )
    public void consume(PaymentEvent event) {
        log.debug("Received payment event type={} orderId={}", event.eventType(), event.orderId());
        notificationApplicationService.handlePaymentEvent(event);
    }
}
