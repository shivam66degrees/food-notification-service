package com.foodplatform.notification.presentation.mapper;

import com.foodplatform.notification.support.NotificationTestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationResponseMapperTest {

    @Test
    void toInboxPageResponse_mapsPageFields() {
        var page = NotificationTestFixtures.samplePage();

        var response = NotificationResponseMapper.toInboxPageResponse(page);

        assertThat(response.items()).hasSize(1);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isEqualTo(1);
    }

    @Test
    void toItemResponse_mapsItemFields() {
        var item = NotificationTestFixtures.sampleItem();

        var response = NotificationResponseMapper.toItemResponse(item);

        assertThat(response.id()).isEqualTo(item.id());
        assertThat(response.title()).isEqualTo(item.title());
        assertThat(response.eventType()).isEqualTo(item.eventType());
    }
}
