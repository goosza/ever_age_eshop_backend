package com.everage.eshop.service;

import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.dto.mapper.CollectionMapper;
import com.everage.eshop.entity.Collection;
import com.everage.eshop.entity.Item;
import com.everage.eshop.repository.CollectionRepository;
import com.everage.eshop.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final ItemRepository itemRepository;
    private final CollectionMapper collectionMapper;

    @Transactional(readOnly = true)
    public List<CollectionDto> getAllCollections() {
        log.debug("Fetching all collections");
        List<Collection> collections = collectionRepository.findAll();
        return collectionMapper.toDtoList(collections);
    }

    @Transactional(readOnly = true)
    public CollectionDto getCollectionById(UUID id) {
        log.debug("Fetching collection with id: {}", id);
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found with id: " + id));
        return collectionMapper.toDto(collection);
    }

    @Transactional
    public CollectionDto createCollection(CollectionDto dto) {
        log.debug("Creating collection with name: {}", dto.name());

        // Check if collection with this name already exists
        collectionRepository.findByName(dto.name()).ifPresent(existing -> {
            log.error("Collection already exists with name: {}", dto.name());
            throw new IllegalArgumentException("Collection with name '" + dto.name() + "' already exists");
        });

        Collection collection = new Collection();
        collection.setName(dto.name());
        collection.setDescription(dto.description());
        collection.setImageUrls(dto.imageUrls() != null
                ? new ArrayList<>(dto.imageUrls())
                : new ArrayList<>());

        Collection saved = collectionRepository.persist(collection); // Hypersistence method
        log.info("Created collection with id: {}", saved.getUuid());

        return collectionMapper.toDto(saved);
    }

    @Transactional
    public CollectionDto updateCollection(UUID id, CollectionDto dto) {
        log.debug("Updating collection with id: {}", id);

        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found with id: " + id));

        // Check if name is being changed and if new name already exists
        if (!collection.getName().equals(dto.name())) {
            collectionRepository.findByName(dto.name()).ifPresent(existing -> {
                log.error("Collection already exists with name: {}", dto.name());
                throw new IllegalArgumentException("Collection with name '" + dto.name() + "' already exists");
            });
        }

        collection.setName(dto.name());
        collection.setDescription(dto.description());
        if (dto.imageUrls() != null) {
            collection.setImageUrls(new ArrayList<>(dto.imageUrls()));
        }

        Collection updated = collectionRepository.merge(collection); // Hypersistence method
        log.info("Updated collection with id: {}", id);

        return collectionMapper.toDto(updated);
    }

    @Transactional
    public void deleteCollection(UUID id) {
        log.debug("Deleting collection with id: {}", id);

        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found with id: " + id));

        // Remove collection reference from all items
        collection.getItems().forEach(item -> {
            log.debug("Removing collection from item: {}", item.getUuid());
            item.setCollection(null);
        });

        collectionRepository.remove(collection); // Hypersistence method
        log.info("Deleted collection with id: {}", id);
    }

    @Transactional
    public void addItemToCollection(UUID collectionId, UUID itemId) {
        log.debug("Adding item {} to collection {}", itemId, collectionId);

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found with id: " + collectionId));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + itemId));

        collection.addItem(item);
        collectionRepository.persist(collection);

        log.info("Added item {} to collection {}", itemId, collectionId);
    }

    @Transactional
    public void removeItemFromCollection(UUID itemId) {
        log.debug("Removing item {} from its collection", itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + itemId));

        if (item.getCollection() != null) {
            UUID collectionId = item.getCollection().getUuid();
            item.getCollection().removeItem(item);
            itemRepository.persist(item);
            log.info("Removed item {} from collection {}", itemId, collectionId);
        } else {
            log.warn("Item {} is not in any collection", itemId);
        }
    }
}