package com.everage.eshop.repository;

import com.everage.eshop.entity.CartItem;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends BaseJpaRepository<CartItem, UUID> {
    Optional<CartItem> findByCartUuidAndItemUuid(UUID cartUuid, UUID itemUuid);
}