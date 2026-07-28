package com.foodplatform.notification.presentation.dto;

import java.util.List;

public record InboxPageResponse(
        List<NotificationItemResponse> items,
        int page,
        int size,
        long totalElements
) {}
