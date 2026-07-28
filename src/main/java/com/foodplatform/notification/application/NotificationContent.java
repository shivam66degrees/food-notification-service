package com.foodplatform.notification.application;

import com.foodplatform.notification.infrastructure.messaging.event.DeliveryEventTypes;
import com.foodplatform.notification.infrastructure.messaging.event.OrderEventTypes;
import com.foodplatform.notification.infrastructure.messaging.event.PaymentEventTypes;

import java.math.BigDecimal;

final class NotificationContent {

    private NotificationContent() {}

    record Copy(String title, String body) {}

    static Copy forPaymentConfirmed(BigDecimal amount, String currency) {
        return new Copy(
                "Payment confirmed",
                "Your payment of " + amount + " " + currency + " was confirmed."
        );
    }

    static Copy forPaymentFailed(String reason) {
        String detail = reason == null || reason.isBlank() ? "Please try again." : reason;
        return new Copy("Payment failed", detail);
    }

    static Copy forEventType(String eventType) {
        return switch (eventType) {
            case OrderEventTypes.ORDER_READY_FOR_DELIVERY -> new Copy(
                    "Order ready",
                    "Your order is ready and will be picked up for delivery soon."
            );
            case DeliveryEventTypes.DELIVERY_ASSIGNED -> new Copy(
                    "Delivery agent assigned",
                    "A delivery agent has been assigned to your order."
            );
            case DeliveryEventTypes.DELIVERY_PICKED_UP -> new Copy(
                    "Order picked up",
                    "Your order has been picked up from the restaurant."
            );
            case DeliveryEventTypes.DELIVERY_IN_TRANSIT -> new Copy(
                    "Out for delivery",
                    "Your order is on the way."
            );
            case DeliveryEventTypes.DELIVERY_DELIVERED -> new Copy(
                    "Delivered",
                    "Your order has been delivered. Enjoy your meal!"
            );
            case PaymentEventTypes.PAYMENT_CONFIRMED, PaymentEventTypes.PAYMENT_FAILED -> throw new IllegalArgumentException(
                    "Use payment-specific copy builders for " + eventType
            );
            default -> null;
        };
    }
}
