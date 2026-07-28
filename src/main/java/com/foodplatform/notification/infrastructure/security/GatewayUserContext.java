package com.foodplatform.notification.infrastructure.security;

import java.util.UUID;

public final class GatewayUserContext {

    private static final ThreadLocal<UUID> USER_ID = new ThreadLocal<>();

    private GatewayUserContext() {}

    public static void setUserId(UUID userId) {
        USER_ID.set(userId);
    }

    public static UUID requireUserId() {
        UUID userId = USER_ID.get();
        if (userId == null) {
            throw new IllegalStateException("Authenticated user id is not available");
        }
        return userId;
    }

    public static void clear() {
        USER_ID.remove();
    }
}
