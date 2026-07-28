package com.foodplatform.notification.application;

import com.foodplatform.notification.infrastructure.persistence.NotificationJpaEntity;
import com.foodplatform.notification.infrastructure.persistence.SpringDataNotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationInboxService {

    private static final int MAX_PAGE_SIZE = 100;

    private final SpringDataNotificationRepository notificationRepository;

    public NotificationInboxService(SpringDataNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public InboxPage listInbox(UUID recipientUserId, int page, int size, boolean unreadOnly) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<NotificationJpaEntity> result = unreadOnly
                ? notificationRepository.findByRecipientUserIdAndReadAtIsNull(recipientUserId, pageRequest)
                : notificationRepository.findByRecipientUserId(recipientUserId, pageRequest);

        return new InboxPage(
                result.getContent().stream().map(NotificationInboxService::toItem).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );
    }

    @Transactional
    public NotificationItem markAsRead(UUID recipientUserId, UUID notificationId) {
        NotificationJpaEntity entity = notificationRepository
                .findByIdAndRecipientUserId(notificationId, recipientUserId)
                .orElseThrow(NotificationNotFoundException::new);

        if (entity.getReadAt() == null) {
            entity.setReadAt(Instant.now());
            entity = notificationRepository.save(entity);
        }

        return toItem(entity);
    }

    static NotificationItem toItem(NotificationJpaEntity entity) {
        return new NotificationItem(
                entity.getId(),
                entity.getEventType(),
                entity.getTitle(),
                entity.getBody(),
                entity.getReadAt(),
                entity.getCreatedAt()
        );
    }

    public record NotificationItem(
            UUID id,
            String eventType,
            String title,
            String body,
            Instant readAt,
            Instant createdAt
    ) {}

    public record InboxPage(
            java.util.List<NotificationItem> items,
            int page,
            int size,
            long totalElements
    ) {}
}
