package com.foodplatform.notification.application;

import com.foodplatform.notification.infrastructure.messaging.event.DeliveryEventTypes;
import com.foodplatform.notification.infrastructure.messaging.event.OrderEventTypes;
import com.foodplatform.notification.infrastructure.messaging.event.PaymentEventTypes;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationContentTest {

    @Test
    void forPaymentConfirmed_buildsCopy() {
        NotificationContent.Copy copy = NotificationContent.forPaymentConfirmed(new BigDecimal("12.99"), "INR");

        assertThat(copy.title()).isEqualTo("Payment confirmed");
        assertThat(copy.body()).contains("12.99");
        assertThat(copy.body()).contains("INR");
    }

    @Test
    void forPaymentFailed_usesReasonWhenPresent() {
        NotificationContent.Copy copy = NotificationContent.forPaymentFailed("Card declined");

        assertThat(copy.title()).isEqualTo("Payment failed");
        assertThat(copy.body()).isEqualTo("Card declined");
    }

    @Test
    void forPaymentFailed_defaultsWhenReasonBlank() {
        NotificationContent.Copy copy = NotificationContent.forPaymentFailed("  ");

        assertThat(copy.body()).isEqualTo("Please try again.");
    }

    @Test
    void forEventType_mapsKnownDeliveryEvents() {
        assertThat(NotificationContent.forEventType(DeliveryEventTypes.DELIVERY_ASSIGNED).title())
                .isEqualTo("Delivery agent assigned");
        assertThat(NotificationContent.forEventType(DeliveryEventTypes.DELIVERY_PICKED_UP).title())
                .isEqualTo("Order picked up");
        assertThat(NotificationContent.forEventType(DeliveryEventTypes.DELIVERY_IN_TRANSIT).title())
                .isEqualTo("Out for delivery");
        assertThat(NotificationContent.forEventType(DeliveryEventTypes.DELIVERY_DELIVERED).title())
                .isEqualTo("Delivered");
        assertThat(NotificationContent.forEventType(OrderEventTypes.ORDER_READY_FOR_DELIVERY).title())
                .isEqualTo("Order ready");
    }

    @Test
    void forEventType_returnsNullForUnknownType() {
        assertThat(NotificationContent.forEventType("UnknownEvent")).isNull();
    }

    @Test
    void forEventType_rejectsPaymentTypes() {
        assertThatThrownBy(() -> NotificationContent.forEventType(PaymentEventTypes.PAYMENT_CONFIRMED))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
