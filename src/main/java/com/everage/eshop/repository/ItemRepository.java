package com.everage.eshop.repository;

import com.everage.eshop.entity.Item;
import org.springframework.stereotype.Repository;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRepository extends BaseJpaRepository<Item, UUID> {
    List<Item> findAll();
}
