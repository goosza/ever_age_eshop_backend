package com.everage.eshop.repository;

import com.everage.eshop.entity.Collection;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollectionRepository extends BaseJpaRepository<Collection, UUID> {
    Optional<Collection> findByUuid(UUID uuid);
    List<Collection> findAll();
    Optional<Collection> findByName(String name);
}
