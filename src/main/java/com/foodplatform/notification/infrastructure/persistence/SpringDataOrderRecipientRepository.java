package com.foodplatform.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataOrderRecipientRepository extends JpaRepository<OrderRecipientJpaEntity, UUID> {

    Optional<OrderRecipientJpaEntity> findByOrderId(UUID orderId);
}
