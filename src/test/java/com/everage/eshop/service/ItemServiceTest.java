package com.everage.eshop.service;

import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.dto.ItemMapper;
import com.everage.eshop.entity.Item;
import com.everage.eshop.exception.ItemNotFoundException;
import com.everage.eshop.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemService itemService;

    @Test
    void getAllItems_ShouldReturnAllItems() {
        // Given
        List<Item> items = List.of(createItem());
        List<ItemDto> itemDtos = List.of(createItemDto());
        
        when(itemRepository.findAll()).thenReturn(items);
        when(itemMapper.toDtoList(items)).thenReturn(itemDtos);

        // When
        List<ItemDto> result = itemService.getAllItems();

        // Then
        assertEquals(1, result.size());
        verify(itemRepository).findAll();
        verify(itemMapper).toDtoList(items);
    }

    @Test
    void getItemById_WhenItemExists_ShouldReturnItem() {
        // Given
        UUID id = UUID.randomUUID();
        Item item = createItem();
        ItemDto itemDto = createItemDto();
        
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        // When
        ItemDto result = itemService.getItemById(id);

        // Then
        assertNotNull(result);
        assertEquals("Test Item", result.name());
        verify(itemRepository).findById(id);
        verify(itemMapper).toDto(item);
    }

    @Test
    void getItemById_WhenItemNotExists_ShouldThrowException() {
        // Given
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ItemNotFoundException.class, () -> itemService.getItemById(id));
        verify(itemRepository).findById(id);
        verifyNoInteractions(itemMapper);
    }

    @Test
    void createItem_ShouldCreateAndReturnItem() {
        // Given
        ItemDto inputDto = createItemDto();
        Item item = createItem();
        ItemDto resultDto = createItemDto();
        
        when(itemMapper.toEntity(inputDto)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(resultDto);

        // When
        ItemDto result = itemService.createItem(inputDto);

        // Then
        assertNotNull(result);
        assertEquals("Test Item", result.name());
        verify(itemMapper).toEntity(inputDto);
        verify(itemRepository).persist(item);
        verify(itemMapper).toDto(item);
    }

    private Item createItem() {
        Item item = new Item();
        item.setUuid(UUID.randomUUID());
        item.setName("Test Item");
        item.setPrice(BigDecimal.valueOf(19.99));
        return item;
    }

    private ItemDto createItemDto() {
        return new ItemDto(UUID.randomUUID(), "Test Item", BigDecimal.valueOf(19.99));
    }
}