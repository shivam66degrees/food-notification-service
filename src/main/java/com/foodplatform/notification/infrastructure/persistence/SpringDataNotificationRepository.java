package com.foodplatform.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    boolean existsBySourceEventId(UUID sourceEventId);
}
