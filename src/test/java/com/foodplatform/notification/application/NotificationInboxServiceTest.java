package com.foodplatform.notification.application;

import com.foodplatform.notification.infrastructure.persistence.NotificationJpaEntity;
import com.foodplatform.notification.infrastructure.persistence.SpringDataNotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationInboxServiceTest {

    @Mock
    private SpringDataNotificationRepository notificationRepository;

    @InjectMocks
    private NotificationInboxService notificationInboxService;

    @Test
    void listInbox_returnsPagedItemsForRecipient() {
        UUID userId = UUID.randomUUID();
        NotificationJpaEntity entity = sampleEntity(userId, "Payment confirmed");
        Page<NotificationJpaEntity> page = new PageImpl<>(List.of(entity), Pageable.ofSize(20), 1);

        when(notificationRepository.findByRecipientUserId(eq(userId), any(Pageable.class))).thenReturn(page);

        NotificationInboxService.InboxPage inbox = notificationInboxService.listInbox(userId, 0, 20, false);

        assertThat(inbox.items()).hasSize(1);
        assertThat(inbox.items().getFirst().title()).isEqualTo("Payment confirmed");
        assertThat(inbox.totalElements()).isEqualTo(1);
    }

    @Test
    void listInbox_unreadOnly_queriesUnreadRepository() {
        UUID userId = UUID.randomUUID();
        Page<NotificationJpaEntity> emptyPage = new PageImpl<>(List.of(), Pageable.ofSize(20), 0);

        when(notificationRepository.findByRecipientUserIdAndReadAtIsNull(eq(userId), any(Pageable.class)))
                .thenReturn(emptyPage);

        notificationInboxService.listInbox(userId, 0, 20, true);

        verify(notificationRepository).findByRecipientUserIdAndReadAtIsNull(eq(userId), any(Pageable.class));
        verify(notificationRepository, never()).findByRecipientUserId(any(), any());
    }

    @Test
    void markAsRead_setsReadAtWhenUnread() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        NotificationJpaEntity entity = sampleEntity(userId, "Delivered");
        entity.setId(notificationId);

        when(notificationRepository.findByIdAndRecipientUserId(notificationId, userId))
                .thenReturn(Optional.of(entity));
        when(notificationRepository.save(entity)).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationInboxService.NotificationItem item =
                notificationInboxService.markAsRead(userId, notificationId);

        ArgumentCaptor<NotificationJpaEntity> captor = ArgumentCaptor.forClass(NotificationJpaEntity.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getReadAt()).isNotNull();
        assertThat(item.readAt()).isNotNull();
    }

    @Test
    void markAsRead_throwsWhenNotificationNotOwned() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        when(notificationRepository.findByIdAndRecipientUserId(notificationId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationInboxService.markAsRead(userId, notificationId))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void markAsRead_whenAlreadyRead_doesNotSaveAgain() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        NotificationJpaEntity entity = sampleEntity(userId, "Delivered");
        entity.setId(notificationId);
        entity.setReadAt(Instant.parse("2026-07-28T01:00:00Z"));

        when(notificationRepository.findByIdAndRecipientUserId(notificationId, userId))
                .thenReturn(Optional.of(entity));

        NotificationInboxService.NotificationItem item =
                notificationInboxService.markAsRead(userId, notificationId);

        verify(notificationRepository, never()).save(any());
        assertThat(item.readAt()).isEqualTo(entity.getReadAt());
    }

    @Test
    void listInbox_clampsPageAndSize() {
        UUID userId = UUID.randomUUID();
        Page<NotificationJpaEntity> emptyPage = new PageImpl<>(List.of(), Pageable.ofSize(100), 0);

        when(notificationRepository.findByRecipientUserId(eq(userId), any(Pageable.class))).thenReturn(emptyPage);

        NotificationInboxService.InboxPage inbox = notificationInboxService.listInbox(userId, -1, 500, false);

        assertThat(inbox.page()).isZero();
        assertThat(inbox.size()).isEqualTo(100);
    }

    private static NotificationJpaEntity sampleEntity(UUID userId, String title) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setRecipientUserId(userId);
        entity.setOrderId(UUID.randomUUID());
        entity.setEventType("PaymentConfirmed");
        entity.setTitle(title);
        entity.setBody("Body");
        entity.setCreatedAt(Instant.parse("2026-07-28T00:00:00Z"));
        return entity;
    }
}
