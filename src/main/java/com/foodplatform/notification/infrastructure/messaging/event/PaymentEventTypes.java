package com.foodplatform.notification.infrastructure.messaging.event;

public final class PaymentEventTypes {

    private PaymentEventTypes() {}

    public static final String PAYMENT_CONFIRMED = "PaymentConfirmed";
    public static final String PAYMENT_FAILED = "PaymentFailed";
}
