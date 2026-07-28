package com.foodplatform.notification.application;

import com.foodplatform.notification.infrastructure.messaging.event.DeliveryEvent;
import com.foodplatform.notification.infrastructure.messaging.event.OrderEvent;
import com.foodplatform.notification.infrastructure.messaging.event.OrderEventTypes;
import com.foodplatform.notification.infrastructure.messaging.event.PaymentEvent;
import com.foodplatform.notification.infrastructure.messaging.event.PaymentEventTypes;
import com.foodplatform.notification.infrastructure.persistence.NotificationJpaEntity;
import com.foodplatform.notification.infrastructure.persistence.OrderRecipientJpaEntity;
import com.foodplatform.notification.infrastructure.persistence.SpringDataNotificationRepository;
import com.foodplatform.notification.infrastructure.persistence.SpringDataOrderRecipientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationApplicationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationApplicationService.class);

    private final SpringDataNotificationRepository notificationRepository;
    private final SpringDataOrderRecipientRepository orderRecipientRepository;

    public NotificationApplicationService(
            SpringDataNotificationRepository notificationRepository,
            SpringDataOrderRecipientRepository orderRecipientRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.orderRecipientRepository = orderRecipientRepository;
    }

    @Transactional
    public void handlePaymentEvent(PaymentEvent event) {
        if (event.eventId() == null || event.orderId() == null || event.customerId() == null) {
            log.warn("Ignoring incomplete payment event type={}", event.eventType());
            return;
        }
        if (notificationRepository.existsBySourceEventId(event.eventId())) {
            log.debug("Skipping duplicate payment event eventId={}", event.eventId());
            return;
        }

        registerOrderRecipient(event.orderId(), event.customerId());

        NotificationContent.Copy copy = switch (event.eventType()) {
            case PaymentEventTypes.PAYMENT_CONFIRMED ->
                    NotificationContent.forPaymentConfirmed(event.amount(), event.currency());
            case PaymentEventTypes.PAYMENT_FAILED ->
                    NotificationContent.forPaymentFailed(event.reason());
            default -> {
                log.debug("Ignoring payment event type={}", event.eventType());
                yield null;
            }
        };

        if (copy == null) {
            return;
        }

        persistNotification(
                event.eventId(),
                event.orderId(),
                event.customerId(),
                event.eventType(),
                copy,
                event.occurredAt()
        );
        log.info("Recorded notification for payment event type={} orderId={}", event.eventType(), event.orderId());
    }

    @Transactional
    public void handleOrderEvent(OrderEvent event) {
        if (event.eventId() == null || event.orderId() == null) {
            log.warn("Ignoring incomplete order event type={}", event.eventType());
            return;
        }
        if (!OrderEventTypes.ORDER_READY_FOR_DELIVERY.equals(event.eventType())) {
            log.debug("Ignoring order event type={}", event.eventType());
            return;
        }
        recordOrderScopedNotification(event.eventId(), event.orderId(), event.eventType(), event.occurredAt());
    }

    @Transactional
    public void handleDeliveryEvent(DeliveryEvent event) {
        if (event.eventId() == null || event.orderId() == null) {
            log.warn("Ignoring incomplete delivery event type={}", event.eventType());
            return;
        }
        recordOrderScopedNotification(event.eventId(), event.orderId(), event.eventType(), event.occurredAt());
    }

    private void recordOrderScopedNotification(
            UUID sourceEventId,
            UUID orderId,
            String eventType,
            Instant occurredAt
    ) {
        if (notificationRepository.existsBySourceEventId(sourceEventId)) {
            log.debug("Skipping duplicate event eventId={}", sourceEventId);
            return;
        }

        NotificationContent.Copy copy = NotificationContent.forEventType(eventType);
        if (copy == null) {
            log.debug("Ignoring unsupported event type={}", eventType);
            return;
        }

        Optional<UUID> recipientId = resolveCustomerId(orderId);
        if (recipientId.isEmpty()) {
            log.warn("No customer mapping for orderId={} eventType={}", orderId, eventType);
            return;
        }

        persistNotification(sourceEventId, orderId, recipientId.get(), eventType, copy, occurredAt);
        log.info("Recorded notification for event type={} orderId={}", eventType, orderId);
    }

    private void registerOrderRecipient(UUID orderId, UUID customerId) {
        if (orderRecipientRepository.findByOrderId(orderId).isPresent()) {
            return;
        }
        OrderRecipientJpaEntity entity = new OrderRecipientJpaEntity();
        entity.setOrderId(orderId);
        entity.setCustomerId(customerId);
        entity.setCreatedAt(Instant.now());
        orderRecipientRepository.save(entity);
    }

    private Optional<UUID> resolveCustomerId(UUID orderId) {
        return orderRecipientRepository.findByOrderId(orderId).map(OrderRecipientJpaEntity::getCustomerId);
    }

    private void persistNotification(
            UUID sourceEventId,
            UUID orderId,
            UUID recipientUserId,
            String eventType,
            NotificationContent.Copy copy,
            Instant occurredAt
    ) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setSourceEventId(sourceEventId);
        entity.setOrderId(orderId);
        entity.setRecipientUserId(recipientUserId);
        entity.setEventType(eventType);
        entity.setTitle(copy.title());
        entity.setBody(copy.body());
        entity.setCreatedAt(occurredAt != null ? occurredAt : Instant.now());
        notificationRepository.save(entity);
    }
}
