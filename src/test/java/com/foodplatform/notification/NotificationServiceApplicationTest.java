package com.foodplatform.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class NotificationServiceApplicationTest {

    @Test
    void mainClassLoads() {
        assertNotNull(NotificationServiceApplication.class);
    }
}
