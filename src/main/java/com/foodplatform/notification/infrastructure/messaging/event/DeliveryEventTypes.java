package com.foodplatform.notification.infrastructure.messaging.event;

public final class DeliveryEventTypes {

    private DeliveryEventTypes() {}

    public static final String DELIVERY_ASSIGNED = "DeliveryAssigned";
    public static final String DELIVERY_PICKED_UP = "DeliveryPickedUp";
    public static final String DELIVERY_IN_TRANSIT = "DeliveryInTransit";
    public static final String DELIVERY_DELIVERED = "DeliveryDelivered";
}
