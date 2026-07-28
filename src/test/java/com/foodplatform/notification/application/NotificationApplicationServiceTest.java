package com.foodplatform.notification.application;

import com.foodplatform.notification.infrastructure.messaging.event.DeliveryEvent;
import com.foodplatform.notification.infrastructure.messaging.event.DeliveryEventTypes;
import com.foodplatform.notification.infrastructure.messaging.event.OrderEvent;
import com.foodplatform.notification.infrastructure.messaging.event.OrderEventTypes;
import com.foodplatform.notification.infrastructure.messaging.event.PaymentEvent;
import com.foodplatform.notification.infrastructure.messaging.event.PaymentEventTypes;
import com.foodplatform.notification.infrastructure.persistence.NotificationJpaEntity;
import com.foodplatform.notification.infrastructure.persistence.OrderRecipientJpaEntity;
import com.foodplatform.notification.infrastructure.persistence.SpringDataNotificationRepository;
import com.foodplatform.notification.infrastructure.persistence.SpringDataOrderRecipientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationApplicationServiceTest {

    @Mock
    private SpringDataNotificationRepository notificationRepository;

    @Mock
    private SpringDataOrderRecipientRepository orderRecipientRepository;

    @InjectMocks
    private NotificationApplicationService notificationApplicationService;

    @Test
    void handlePaymentConfirmed_persistsNotificationAndOrderRecipient() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        PaymentEvent event = new PaymentEvent(
                eventId,
                PaymentEventTypes.PAYMENT_CONFIRMED,
                Instant.parse("2026-07-28T00:00:00Z"),
                UUID.randomUUID(),
                orderId,
                customerId,
                new BigDecimal("12.99"),
                "INR",
                null
        );

        when(notificationRepository.existsBySourceEventId(eventId)).thenReturn(false);
        when(orderRecipientRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        notificationApplicationService.handlePaymentEvent(event);

        verify(orderRecipientRepository).save(any(OrderRecipientJpaEntity.class));

        ArgumentCaptor<NotificationJpaEntity> captor = ArgumentCaptor.forClass(NotificationJpaEntity.class);
        verify(notificationRepository).save(captor.capture());
        NotificationJpaEntity saved = captor.getValue();
        assertThat(saved.getRecipientUserId()).isEqualTo(customerId);
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getSourceEventId()).isEqualTo(eventId);
        assertThat(saved.getEventType()).isEqualTo(PaymentEventTypes.PAYMENT_CONFIRMED);
        assertThat(saved.getTitle()).isEqualTo("Payment confirmed");
    }

    @Test
    void handlePaymentEvent_skipsDuplicateSourceEvent() {
        UUID eventId = UUID.randomUUID();
        PaymentEvent event = new PaymentEvent(
                eventId,
                PaymentEventTypes.PAYMENT_CONFIRMED,
                Instant.now(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                "INR",
                null
        );

        when(notificationRepository.existsBySourceEventId(eventId)).thenReturn(true);

        notificationApplicationService.handlePaymentEvent(event);

        verify(notificationRepository, never()).save(any());
        verify(orderRecipientRepository, never()).save(any());
    }

    @Test
    void handleDeliveryEvent_usesOrderRecipientMapping() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        DeliveryEvent event = new DeliveryEvent(
                eventId,
                DeliveryEventTypes.DELIVERY_DELIVERED,
                Instant.now(),
                orderId
        );

        OrderRecipientJpaEntity mapping = new OrderRecipientJpaEntity();
        mapping.setOrderId(orderId);
        mapping.setCustomerId(customerId);

        when(notificationRepository.existsBySourceEventId(eventId)).thenReturn(false);
        when(orderRecipientRepository.findByOrderId(orderId)).thenReturn(Optional.of(mapping));

        notificationApplicationService.handleDeliveryEvent(event);

        ArgumentCaptor<NotificationJpaEntity> captor = ArgumentCaptor.forClass(NotificationJpaEntity.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getRecipientUserId()).isEqualTo(customerId);
        assertThat(captor.getValue().getTitle()).isEqualTo("Delivered");
    }

    @Test
    void handleDeliveryEvent_skipsWhenCustomerMappingMissing() {
        UUID orderId = UUID.randomUUID();
        DeliveryEvent event = new DeliveryEvent(
                UUID.randomUUID(),
                DeliveryEventTypes.DELIVERY_ASSIGNED,
                Instant.now(),
                orderId
        );

        when(notificationRepository.existsBySourceEventId(event.eventId())).thenReturn(false);
        when(orderRecipientRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        notificationApplicationService.handleDeliveryEvent(event);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void handleOrderEvent_recordsOrderReadyNotification() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        OrderEvent event = new OrderEvent(
                eventId,
                OrderEventTypes.ORDER_READY_FOR_DELIVERY,
                Instant.now(),
                orderId
        );

        OrderRecipientJpaEntity mapping = new OrderRecipientJpaEntity();
        mapping.setOrderId(orderId);
        mapping.setCustomerId(customerId);

        when(notificationRepository.existsBySourceEventId(eventId)).thenReturn(false);
        when(orderRecipientRepository.findByOrderId(orderId)).thenReturn(Optional.of(mapping));

        notificationApplicationService.handleOrderEvent(event);

        ArgumentCaptor<NotificationJpaEntity> captor = ArgumentCaptor.forClass(NotificationJpaEntity.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(OrderEventTypes.ORDER_READY_FOR_DELIVERY);
    }
}
