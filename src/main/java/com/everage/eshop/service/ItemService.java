package com.everage.eshop.service;

import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.dto.mapper.ItemMapper;
import com.everage.eshop.entity.Item;
import com.everage.eshop.exception.ItemAlreadyExistsException;
import com.everage.eshop.exception.ItemNotFoundException;
import com.everage.eshop.exception.InvalidItemStatusException;
import com.everage.eshop.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.everage.eshop.entity.ItemStatus;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Transactional(readOnly = true)
    public List<ItemDto> getAllItems() {
        log.info("Fetching all items");
        List<ItemDto> items = itemMapper.toDtoList(itemRepository.findAll());
        log.info("Found {} items", items.size());
        return items;
    }

    @Transactional(readOnly = true)
    public ItemDto getItemById(UUID uuid) {
        log.info("Fetching item by uuid: {}", uuid);
        Item item = itemRepository.findById(uuid)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with uuid: " + uuid));
        log.info("Found item: {}", item.getName());
        return itemMapper.toDto(item);
    }

    @Transactional
    public ItemDto createItem(ItemDto itemDto) {
        if (itemRepository.findByName(itemDto.name()).isPresent()) {
            throw new ItemAlreadyExistsException("Item with name " + itemDto.name() + " already exists");
        }
        log.info("Creating new item: {}", itemDto.name());
        Item item = itemMapper.toEntity(itemDto);
        
        // Validate item status against quantity
        validateItemStatus(item, itemDto.status());
        
        itemRepository.persist(item);
        ItemDto result = itemMapper.toDto(item);
        log.info("Item created successfully with uuid: {}, name: {}", result.uuid(), result.name());
        return result;
    }

    @Transactional
    public ItemDto updateItem(UUID uuid, ItemDto itemDto) {
        log.info("Updating item with uuid: {}, name: {}", uuid, itemDto.name());
        Item item = itemRepository.findById(uuid)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with uuid: " + uuid));

        itemRepository.findByName(itemDto.name())
                .filter(existingItem -> !existingItem.getUuid().equals(uuid))
                .ifPresent(existingItem -> {
                    throw new ItemAlreadyExistsException("Name already taken");
                });

        item.setName(itemDto.name());
        item.setDescription(itemDto.description());
        item.setPrice(itemDto.price());
        item.setQuantity(itemDto.quantity());
        if (itemDto.imageUrls() != null) {
            item.setImageUrls(new ArrayList<>(itemDto.imageUrls()));
        } else {
            // If null, clear
            item.setImageUrls(new ArrayList<>());
        }
        // Validate item status against quantity
        validateItemStatus(item, itemDto.status());
//        itemRepository.persist(item);
        ItemDto result = itemMapper.toDto(item);
        log.info("Item updated successfully with uuid: {}, name: {}", result.uuid(), result.name());
        log.info("Updated info: name={}, description={}, price={}, status={}, quantity={}, images count={}",
                result.name(), result.description(), result.price(), result.status(),
                result.quantity(), result.imageUrls() != null ? result.imageUrls().size() : 0);
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
}
