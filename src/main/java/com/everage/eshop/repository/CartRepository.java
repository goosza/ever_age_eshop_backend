package com.everage.eshop.repository;

import com.everage.eshop.entity.Cart;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CartRepository extends BaseJpaRepository<Cart, UUID> {
}