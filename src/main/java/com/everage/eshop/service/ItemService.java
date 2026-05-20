package com.everage.eshop.service;

import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.dto.ItemRequest;
import com.everage.eshop.dto.mapper.ItemMapper;
import com.everage.eshop.entity.Item;
import com.everage.eshop.entity.ItemStatus;
import com.everage.eshop.exception.item.ItemAlreadyExistsException;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.exception.item.InvalidItemStatusException;
import com.everage.eshop.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public List<ItemDto> getAllItems() {
        log.info("Fetching all items");
        List<ItemDto> items = itemMapper.toDtoList(itemRepository.findAll())
                .stream().map(this::resolveUrls).toList();
        log.info("Found {} items", items.size());
        return items;
    }

    @Transactional(readOnly = true)
    public ItemDto getItemById(UUID uuid) {
        log.info("Fetching item by uuid: {}", uuid);
        Item item = itemRepository.findById(uuid)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with uuid: " + uuid));
        log.info("Found item: {}", item.getName());
        return resolveUrls(itemMapper.toDto(item));
    }

    @Transactional
    public ItemDto createItem(ItemRequest request, List<MultipartFile> images) {
        if (itemRepository.findByName(request.name()).isPresent()) {
            throw new ItemAlreadyExistsException("Item with name " + request.name() + " already exists");
        }
        log.info("Creating new item: {}", request.name());

        List<String> imageUrls = uploadImages(images);

        Item item = new Item();
        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setQuantity(request.quantity());
        item.setColor(request.color());
        item.setImageUrls(imageUrls);
        validateItemStatus(item, request.status());

        itemRepository.persist(item);
        ItemDto result = resolveUrls(itemMapper.toDto(item));
        log.info("Item created successfully with uuid: {}, name: {}", result.uuid(), result.name());
        return result;
    }

    @Transactional
    public ItemDto updateItem(UUID uuid, ItemRequest request, List<MultipartFile> images) {
        log.info("Updating item with uuid: {}", uuid);
        Item item = itemRepository.findById(uuid)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with uuid: " + uuid));

        itemRepository.findByName(request.name())
                .filter(existing -> !existing.getUuid().equals(uuid))
                .ifPresent(existing -> {
                    throw new ItemAlreadyExistsException("Name already taken");
                });

        // Delete images that are no longer in the "keep" list
        List<String> urlsToKeep = request.existingImageUrls() != null ? request.existingImageUrls() : List.of();
        List<String> urlsToDelete = item.getImageUrls().stream()
                .filter(url -> !urlsToKeep.contains(url))
                .toList();
        storageService.deleteAll(urlsToDelete);

        // Upload new images and merge with kept ones
        List<String> newUrls = uploadImages(images);
        List<String> mergedUrls = new ArrayList<>(urlsToKeep);
        mergedUrls.addAll(newUrls);

        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setQuantity(request.quantity());
        item.setColor(request.color());
        item.setImageUrls(mergedUrls);
        validateItemStatus(item, request.status());

        ItemDto result = resolveUrls(itemMapper.toDto(item));
        log.info("Item updated successfully with uuid: {}, name: {}", result.uuid(), result.name());
        return result;
    }

    /**
     * Validates item status against quantity
     * @param item {@link Item}
     * @param requestedStatus {@link ItemStatus}
     */
    private void validateItemStatus(Item item, ItemStatus requestedStatus) {
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            if (requestedStatus != ItemStatus.OUT_OF_STOCK) {
                throw new InvalidItemStatusException(
                    "Item with zero quantity must have OUT_OF_STOCK status, but got: " + requestedStatus);
            }
        } else {
            if (requestedStatus == ItemStatus.OUT_OF_STOCK) {
                throw new InvalidItemStatusException(
                    "Item with positive quantity (" + item.getQuantity() + ") cannot have OUT_OF_STOCK status");
            }
        }
        
        // If validation passes, set the requested status
        item.setStatus(requestedStatus != null ? requestedStatus : ItemStatus.ACTIVE);
    }

    /**
     * Decreases item amount (for ex., after purchase)
     * @param uuid {@link UUID}
     * @param amount {@link Integer}
     * @return {@link ItemDto}
     */
    @Transactional
    public ItemDto decreaseQuantity(UUID uuid, Integer amount) {
        log.info("Decreasing quantity for item {} by {}", uuid, amount);
        Item item = itemRepository.findById(uuid)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with uuid: " + uuid));
        
        int newQuantity = Math.max(0, item.getQuantity() - amount);
        item.setQuantity(newQuantity);
        
        // Validate status after quantity change
        if (newQuantity <= 0 && item.getStatus() != ItemStatus.OUT_OF_STOCK) {
            item.setStatus(ItemStatus.OUT_OF_STOCK);
            log.info("Item {} status automatically changed to OUT_OF_STOCK due to zero quantity", item.getName());
        }
        
        ItemDto result = itemMapper.toDto(item);
        log.info("Item quantity updated: {} -> {}, status: {}", 
                item.getQuantity() + amount, newQuantity, result.status());
        return result;
    }

    @Transactional
    public void deleteItem(UUID uuid) {
        log.info("Deleting item with uuid: {}", uuid);

        Item item = itemRepository.findById(uuid)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with uuid: " + uuid));

        // Delete images from R2
        storageService.deleteAll(item.getImageUrls());

        // Remove item from collection if it belongs to one
        if (item.getCollection() != null) {
            log.info("Removing item {} from collection {}", uuid, item.getCollection().getUuid());
            item.getCollection().removeItem(item);
        }

        itemRepository.delete(item);
        log.info("Item deleted successfully with uuid: {}", uuid);
    }

    @Transactional(readOnly = true)
    public List<ItemDto> getItemsByCollectionUuid(UUID collectionUuid) {
        log.info("Fetching items for collection uuid: {}", collectionUuid);
        List<ItemDto> items = itemMapper.toDtoList(
                itemRepository.findByCollectionUuid(collectionUuid))
                .stream().map(this::resolveUrls).toList();
        log.info("Found {} items for collection {}", items.size(), collectionUuid);
        return items;
    }

    /** Replaces storage keys in imageUrls with full public URLs. */
    private ItemDto resolveUrls(ItemDto dto) {
        return new ItemDto(
                dto.uuid(), dto.name(), dto.description(),
                storageService.toPublicUrls(dto.imageUrls()),
                dto.price(), dto.weight(), dto.status(), dto.quantity(), dto.color(),
                dto.collection()
        );
    }

    private List<String> uploadImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return new ArrayList<>();
        return images.stream()
                .filter(f -> f != null && !f.isEmpty())
                .map(f -> storageService.upload(f, "items"))
                .toList();
    }
}
