package com.foodplatform.notification.infrastructure.security;

import com.foodplatform.notification.support.NotificationTestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GatewayUserContextTest {

    @AfterEach
    void clearContext() {
        GatewayUserContext.clear();
    }

    @Test
    void requireUserId_returnsSetUserId() {
        GatewayUserContext.setUserId(NotificationTestFixtures.CUSTOMER_ID);

        assertThat(GatewayUserContext.requireUserId()).isEqualTo(NotificationTestFixtures.CUSTOMER_ID);
    }

    @Test
    void requireUserId_withoutContext_throws() {
        assertThatThrownBy(GatewayUserContext::requireUserId)
                .isInstanceOf(IllegalStateException.class);
    }
}
