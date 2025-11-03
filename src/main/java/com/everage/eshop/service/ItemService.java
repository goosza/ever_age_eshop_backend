package com.everage.eshop.service;

import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.dto.ItemMapper;
import com.everage.eshop.entity.Item;
import com.everage.eshop.exception.ItemAlreadyExistsException;
import com.everage.eshop.exception.ItemNotFoundException;
import com.everage.eshop.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    public ItemDto getItemById(UUID id) {
        log.info("Fetching item by id: {}", id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));
        log.info("Found item: {}", item.getName());
        return itemMapper.toDto(item);
    }

    @Transactional
    public ItemDto createItem(ItemDto itemDto) {
        if (itemRepository.findByName(itemDto.name()) != null) {
            throw new ItemAlreadyExistsException("Item with name " + itemDto.name() + " already exists");
        };
        log.info("Creating new item: {}", itemDto.name());
        Item item = itemMapper.toEntity(itemDto);
        
        // Automatic item status control while creating new item
        updateItemStatus(item, itemDto.status());
        
        itemRepository.persist(item);
        ItemDto result = itemMapper.toDto(item);
        log.info("Item created successfully with id: {}, name: {}", result.uuid(), result.name());
        return result;
    }

    @Transactional
    public ItemDto updateItem(UUID id, ItemDto itemDto) {
        log.info("Updating item with id: {}, name: {}", id, itemDto.name());
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));

        Item existingByName = itemRepository.findByName(itemDto.name());
        if (existingByName != null && !existingByName.getUuid().equals(id)) {
            throw new ItemAlreadyExistsException("Name already taken");
        }
        item.setName(itemDto.name());
        item.setDescription(itemDto.description());
        item.setPrice(itemDto.price());
        item.setQuantity(itemDto.quantity());
        
        // Automatic item status control based on amount
        updateItemStatus(item, itemDto.status());
//        itemRepository.persist(item);
        ItemDto result = itemMapper.toDto(item);
        log.info("Item updated successfully with id: {}, name: {}", result.uuid(), result.name());
        log.info("Updated info: name={}, description={}, price={}, status={}, quantity={}",
                result.name(), result.description(), result.price(), result.status(), result.quantity());
        return result;
    }

    /**
     * Automatic item status control based on amount
     * @param {@link Item} item
     * @param {@link ItemStatus} requestedStatus
     */
    private void updateItemStatus(Item item, ItemStatus requestedStatus) {
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            // If amount is 0 or below 0, setting OUT_OF_STOCK
            item.setStatus(ItemStatus.OUT_OF_STOCK);
            log.info("Item {} status automatically set to OUT_OF_STOCK due to zero quantity", item.getName());
        } else if (item.getQuantity() > 0 && requestedStatus == ItemStatus.OUT_OF_STOCK) {
            // If amount is bigger than 0, but requesting OUT_OF_STOCK, setting ACTIVE
            item.setStatus(ItemStatus.ACTIVE);
            log.info("Item {} status automatically set to ACTIVE due to positive quantity", item.getName());
        } else {
            // In other cases using requested status
            item.setStatus(requestedStatus != null ? requestedStatus : ItemStatus.ACTIVE);
        }
    }

    /**
     * Decreases item amount (for ex., after purchase)
     * @param {@link UUID} id
     * @param {@link Integer} amount
     * @return {@link ItemDto}
     */
    @Transactional
    public ItemDto decreaseQuantity(UUID id, Integer amount) {
        log.info("Decreasing quantity for item {} by {}", id, amount);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));
        
        int newQuantity = Math.max(0, item.getQuantity() - amount);
        item.setQuantity(newQuantity);
        
        // Automatically updating status
        updateItemStatus(item, item.getStatus());
        
        ItemDto result = itemMapper.toDto(item);
        log.info("Item quantity updated: {} -> {}, status: {}", 
                item.getQuantity() + amount, newQuantity, result.status());
        return result;
    }
}
