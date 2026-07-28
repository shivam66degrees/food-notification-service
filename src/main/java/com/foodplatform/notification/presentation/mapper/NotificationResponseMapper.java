package com.foodplatform.notification.presentation.mapper;

import com.foodplatform.notification.application.NotificationInboxService;
import com.foodplatform.notification.presentation.dto.InboxPageResponse;
import com.foodplatform.notification.presentation.dto.NotificationItemResponse;

public final class NotificationResponseMapper {

    private NotificationResponseMapper() {}

    public static InboxPageResponse toInboxPageResponse(NotificationInboxService.InboxPage page) {
        return new InboxPageResponse(
                page.items().stream().map(NotificationResponseMapper::toItemResponse).toList(),
                page.page(),
                page.size(),
                page.totalElements()
        );
    }

    public static NotificationItemResponse toItemResponse(NotificationInboxService.NotificationItem item) {
        return new NotificationItemResponse(
                item.id(),
                item.eventType(),
                item.title(),
                item.body(),
                item.readAt(),
                item.createdAt()
        );
    }
}
