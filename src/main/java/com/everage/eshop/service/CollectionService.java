package com.everage.eshop.service;

import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.dto.CollectionMultipartRequest;
import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.dto.mapper.CollectionMapper;
import com.everage.eshop.dto.mapper.ItemMapper;
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
import org.springframework.web.multipart.MultipartFile;

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
    private final ItemMapper itemMapper;
    private final StorageService storageService;

    /**
     * Retrieves all collections without items.
     *
     * @return list of {@link CollectionDto} without items
     */
    @Transactional(readOnly = true)
    public List<CollectionDto> getAllCollections() {
        log.debug("Fetching all collections");
        return collectionMapper.toDtoList(collectionRepository.findAll())
                .stream().map(this::resolveUrls).toList();
    }

    /**
     * Retrieves a collection by UUID with all its items.
     *
     * @param uuid collection UUID
     * @return {@link CollectionDto} with items (items without collection reference to avoid circular dependency)
     * @throws CollectionNotFoundException if collection not found
     */
    @Transactional(readOnly = true)
    public CollectionDto getCollectionByUuid(UUID uuid) {
        log.debug("Fetching collection with uuid: {}", uuid);
        Collection collection = collectionRepository.findById(uuid)
                .orElseThrow(() -> new CollectionNotFoundException("Collection not found with uuid: " + uuid));

        CollectionDto dto = collectionMapper.toDto(collection);
        List<ItemDto> items = itemMapper.toDtoListWithoutCollection(collection.getItems());

        return resolveUrls(new CollectionDto(
                dto.uuid(), dto.name(), dto.description(), dto.imageUrls(),
                items, dto.createdAt(), dto.updatedAt()
        ));
    }

    /**
     * Creates a new collection.
     *
     * @param request collection request data
     * @param images  image files to upload
     * @return created {@link CollectionDto} without items
     * @throws CollectionAlreadyExistsException if collection with the same name already exists
     */
    @Transactional
    public CollectionDto createCollection(CollectionMultipartRequest request, List<MultipartFile> images) {
        log.debug("Creating collection with name: {}", request.name());

        collectionRepository.findByName(request.name()).ifPresent(existing -> {
            log.error("Collection already exists with name: {}", request.name());
            throw new CollectionAlreadyExistsException("Collection with name '" + request.name() + "' already exists");
        });

        List<String> imageUrls = uploadImages(images);

        Collection collection = new Collection();
        collection.setName(request.name());
        collection.setDescription(request.description());
        collection.setImageUrls(imageUrls);

        Collection savedCollection = collectionRepository.persist(collection);
        log.info("Created collection with uuid: {}", savedCollection.getUuid());

        return resolveUrls(collectionMapper.toDto(savedCollection));
    }

    /**
     * Updates an existing collection.
     *
     * @param uuid    collection UUID
     * @param request updated collection data
     * @param images  new image files to upload
     * @return updated {@link CollectionDto} with items
     * @throws CollectionNotFoundException      if collection not found
     * @throws CollectionAlreadyExistsException if new name already exists
     */
    @Transactional
    public CollectionDto updateCollection(UUID uuid, CollectionMultipartRequest request, List<MultipartFile> images) {
        log.debug("Updating collection with uuid: {}", uuid);

        Collection collection = collectionRepository.findById(uuid)
                .orElseThrow(() -> new CollectionNotFoundException("Collection not found with uuid: " + uuid));

        if (!collection.getName().equals(request.name())) {
            collectionRepository.findByName(request.name()).ifPresent(existing -> {
                log.error("Collection already exists with name: {}", request.name());
                throw new CollectionAlreadyExistsException("Collection with name '" + request.name() + "' already exists");
            });
        }

        // Delete images that are no longer in the "keep" list
        List<String> urlsToKeep = request.existingImageUrls() != null ? request.existingImageUrls() : List.of();
        List<String> urlsToDelete = collection.getImageUrls().stream()
                .filter(url -> !urlsToKeep.contains(url))
                .toList();
        storageService.deleteAll(urlsToDelete);

        List<String> newUrls = uploadImages(images);
        List<String> mergedUrls = new ArrayList<>(urlsToKeep);
        mergedUrls.addAll(newUrls);

        collection.setName(request.name());
        collection.setDescription(request.description());
        collection.setImageUrls(mergedUrls);

        Collection updated = collectionRepository.merge(collection);
        log.info("Updated collection with uuid: {}", uuid);

        CollectionDto updatedDto = collectionMapper.toDto(updated);
        List<ItemDto> items = itemMapper.toDtoListWithoutCollection(updated.getItems());

        return resolveUrls(new CollectionDto(
                updatedDto.uuid(), updatedDto.name(), updatedDto.description(), updatedDto.imageUrls(),
                items, updatedDto.createdAt(), updatedDto.updatedAt()
        ));
    }

    /**
     * Deletes a collection.
     * All items in this collection will have their collection reference removed.
     * All collection images will be deleted from R2.
     *
     * @param uuid collection UUID
     * @throws CollectionNotFoundException if collection not found
     */
    @Transactional
    public void deleteCollection(UUID uuid) {
        log.debug("Deleting collection with uuid: {}", uuid);

        Collection collection = collectionRepository.findById(uuid)
                .orElseThrow(() -> new CollectionNotFoundException("Collection not found with uuid: " + uuid));

        // Delete collection images from R2
        storageService.deleteAll(collection.getImageUrls());

        // Remove collection reference from all items
        collection.getItems().forEach(item -> {
            log.debug("Removing collection from item: {}", item.getUuid());
            item.setCollection(null);
        });

        collectionRepository.delete(collection);
        log.info("Deleted collection with uuid: {}", uuid);
    }

    /**
     * Adds an item to a collection.
     *
     * @param collectionUuid collection UUID
     * @param itemUuid item UUID
     * @return updated {@link CollectionDto} with items
     * @throws CollectionNotFoundException if collection not found
     * @throws ItemNotFoundException if item not found
     */
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

        // Return collection with updated items
        CollectionDto dto = collectionMapper.toDto(collection);
        List<ItemDto> items = itemMapper.toDtoListWithoutCollection(collection.getItems());

        return resolveUrls(new CollectionDto(
                dto.uuid(), dto.name(), dto.description(), dto.imageUrls(),
                items, dto.createdAt(), dto.updatedAt()
        ));
    }

    /**
     * Removes an item from a collection.
     *
     * @param collectionUuid collection UUID
     * @param itemUuid item UUID
     * @return updated {@link CollectionDto} with items
     * @throws CollectionNotFoundException if collection not found
     * @throws ItemNotFoundException if item not found
     * @throws ItemNotInCollectionException if item is not in the collection
     */
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

        // Return collection with updated items list
        CollectionDto dto = collectionMapper.toDto(collection);
        List<ItemDto> items = itemMapper.toDtoListWithoutCollection(collection.getItems());

        return resolveUrls(new CollectionDto(
                dto.uuid(), dto.name(), dto.description(), dto.imageUrls(),
                items, dto.createdAt(), dto.updatedAt()
        ));
    }

    /** Replaces storage keys in imageUrls with full public URLs. */
    private CollectionDto resolveUrls(CollectionDto dto) {
        return new CollectionDto(
                dto.uuid(), dto.name(), dto.description(),
                storageService.toPublicUrls(dto.imageUrls()),
                dto.items(), dto.createdAt(), dto.updatedAt()
        );
    }

    private List<String> uploadImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return new ArrayList<>();
        return images.stream()
                .filter(f -> f != null && !f.isEmpty())
                .map(f -> storageService.upload(f, "collections"))
                .toList();
    }
}