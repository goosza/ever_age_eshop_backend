package com.everage.eshop.repository;

import com.everage.eshop.entity.Shipping;
import com.everage.eshop.entity.ShippingStatus;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShippingRepository extends BaseJpaRepository<Shipping, UUID> {
    Optional<Shipping> findByOrderUuid(UUID orderUuid);
    List<Shipping> findByStatus(ShippingStatus status);
    List<Shipping> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Shipping> findByTrackingNumber(String trackingNumber);
}
