package com.everage.eshop.repository;

import com.everage.eshop.entity.Payment;
import com.everage.eshop.entity.PaymentStatus;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends BaseJpaRepository<Payment, UUID> {
    Optional<Payment> findByUuid(UUID uuid);
    Optional<Payment> findByOrderUuid(UUID orderUuid);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
