package com.everage.eshop.service;

import com.everage.eshop.dto.CollectionDetailDto;
import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.dto.CollectionRequest;
import com.everage.eshop.dto.mapper.CollectionMapper;
import com.everage.eshop.entity.Collection;
import com.everage.eshop.entity.Item;
import com.everage.eshop.exception.collection.CollectionAlreadyExistsException;
import com.everage.eshop.exception.collection.CollectionNotFoundException;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.exception.item.ItemNotInCollectionException;
import com.everage.eshop.repository.CollectionRepository;
import com.everage.eshop.repository.ItemRepository;
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
    public CollectionDetailDto getCollectionByUuid(UUID uuid) {
        log.debug("Fetching collection with uuid: {}", uuid);
        Collection collection = collectionRepository.findById(uuid)
                .orElseThrow(() -> new CollectionNotFoundException("Collection not found with uuid: " + uuid));
        return collectionMapper.toDetailDto(collection);
    }

    @Transactional
    public CollectionDto createCollection(CollectionRequest dto) {
        log.debug("Creating collection with name: {}", dto.name());

        // Check if collection with this name already exists
        collectionRepository.findByName(dto.name()).ifPresent(existing -> {
            log.error("Collection already exists with name: {}", dto.name());
            throw new CollectionAlreadyExistsException("Collection with name '" + dto.name() + "' already exists");
        });

        Collection collection = new Collection();
        collection.setName(dto.name());
        collection.setDescription(dto.description());
        collection.setImageUrls(dto.imageUrls() != null
                ? new ArrayList<>(dto.imageUrls())
                : new ArrayList<>());

        Collection savedCollection = collectionRepository.persist(collection); // Hypersistence method
        log.info("Created collection with uuid: {}", savedCollection.getUuid());

        return collectionMapper.toDto(savedCollection);
    }

    @Transactional
    public CollectionDto updateCollection(UUID uuid, CollectionDto dto) {
        log.debug("Updating collection with uuid: {}", uuid);

        Collection collection = collectionRepository.findById(uuid)
                .orElseThrow(() -> new CollectionNotFoundException("Collection not found with uuid: " + uuid));

        // Check if name is being changed and if new name already exists
        if (!collection.getName().equals(dto.name())) {
            collectionRepository.findByName(dto.name()).ifPresent(existing -> {
                log.error("Collection already exists with name: {}", dto.name());
                throw new CollectionAlreadyExistsException("Collection with name '" + dto.name() + "' already exists");
            });
        }

        collection.setName(dto.name());
        collection.setDescription(dto.description());
        if (dto.imageUrls() != null) {
            collection.setImageUrls(new ArrayList<>(dto.imageUrls()));
        }

        Collection updated = collectionRepository.merge(collection); // Hypersistence method
        log.info("Updated collection with uuid: {}", uuid);

        return collectionMapper.toDto(updated);
    }

    @Transactional
    public void deleteCollection(UUID uuid) {
        log.debug("Deleting collection with uuid: {}", uuid);

        Collection collection = collectionRepository.findById(uuid)
                .orElseThrow(() -> new CollectionNotFoundException("Collection not found with uuid: " + uuid));

        // Remove collection reference from all items
        collection.getItems().forEach(item -> {
            log.debug("Removing collection from item: {}", item.getUuid());
            item.setCollection(null);
        });

        collectionRepository.delete(collection); // Hypersistence method
        log.info("Deleted collection with uuid: {}", uuid);
    }

    @Transactional
    public CollectionDto addItemToCollection(UUID collectionUuid, UUID itemUuid) {
        log.debug("Adding item {} to collection {}", itemUuid, collectionUuid);

        Collection collection = collectionRepository.findById(collectionUuid)
                .orElseThrow(() -> new CollectionNotFoundException("Collection not found with id: " + collectionUuid));

        Item item = itemRepository.findById(itemUuid)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + itemUuid));

        collection.addItem(item);
        collectionRepository.persist(collection);

        log.info("Added item {} to collection {}", itemUuid, collectionUuid);

        return collectionMapper.toDto(collection);
    }

    @Transactional
    public CollectionDto removeItemFromCollection(UUID collectionUuid, UUID itemUuid) {
        log.debug("Removing item {} from collection {}", itemUuid, collectionUuid);

        Collection collection = collectionRepository.findById(collectionUuid)
                .orElseThrow(() -> new CollectionNotFoundException("Collection not found with id: " + collectionUuid));

        Item item = itemRepository.findById(itemUuid)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + itemUuid));

        if (!collection.getItems().contains(item)) {
            log.warn("Item {} is not in collection {}", itemUuid, collectionUuid);
            throw new ItemNotInCollectionException("Item " + itemUuid + " is not in collection " + collectionUuid);
        }

        collection.removeItem(item);
        collectionRepository.persist(collection);

        log.info("Removed item {} from collection {}", itemUuid, collectionUuid);

        return collectionMapper.toDto(collection);
    }
}