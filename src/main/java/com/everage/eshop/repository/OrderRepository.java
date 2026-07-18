package com.everage.eshop.repository;

import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.OrderStatus;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends BaseJpaRepository<Order, UUID> {
    Optional<Order> findByUuid(UUID uuid);
    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByStripeSessionId(String stripeSessionId);
    List<Order> findByEmailOrderByCreatedAtDesc(String email);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findAllByOrderByCreatedAtDesc();
    Page<Order> findAll(Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
