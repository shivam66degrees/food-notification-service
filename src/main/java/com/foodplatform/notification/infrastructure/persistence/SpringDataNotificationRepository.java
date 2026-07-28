package com.foodplatform.notification.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    boolean existsBySourceEventId(UUID sourceEventId);

    Page<NotificationJpaEntity> findByRecipientUserId(UUID recipientUserId, Pageable pageable);

    Page<NotificationJpaEntity> findByRecipientUserIdAndReadAtIsNull(UUID recipientUserId, Pageable pageable);

    Optional<NotificationJpaEntity> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);
}
