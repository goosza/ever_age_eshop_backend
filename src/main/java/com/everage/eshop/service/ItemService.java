package com.everage.eshop.service;

import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.dto.ItemMapper;
import com.everage.eshop.entity.Item;
import com.everage.eshop.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

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
        ItemDto item = itemMapper.toDto(itemRepository
                .findById(id)
                .orElse(null)
        );
        if (item != null) {
            log.info("Found item: {}", item.name());
        } else {
            log.warn("Item not found with id: {}", id);
        }
        return item;
    }

    @Transactional
    public ItemDto createItem(ItemDto itemDto) {
        log.info("Creating new item: {}", itemDto.name());
        Item item = itemMapper.toEntity(itemDto);
        itemRepository.persist(item);
        ItemDto result = itemMapper.toDto(item);
        log.info("Item created successfully with id: {}, name: {}", result.uuid(), result.name());
        return result;
    }
}
