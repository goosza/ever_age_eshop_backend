package com.everage.eshop.service;

import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.dto.ItemMapper;
import com.everage.eshop.entity.Item;
import com.everage.eshop.repository.ItemRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

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
        return itemMapper.toDtoList(itemRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ItemDto getItemById(UUID id) {
        return itemMapper.toDto(itemRepository
                .findById(id)
                .orElse(null)
        );
    }

    @Transactional
    public ItemDto createItem(ItemDto itemDto) {
        Item item = itemMapper.toEntity(itemDto);
        itemRepository.persist(item);
        return itemMapper.toDto(item);
    }
}
