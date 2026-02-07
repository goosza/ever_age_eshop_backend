package com.everage.eshop.service;

import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.dto.mapper.ItemMapper;
import com.everage.eshop.entity.Item;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.exception.item.ItemAlreadyExistsException;
import com.everage.eshop.exception.item.InvalidItemStatusException;
import com.everage.eshop.entity.ItemStatus;
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

import static org.junit.jupiter.api.Assertions.*;
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
        
        when(itemRepository.findByName(inputDto.name())).thenReturn(Optional.empty());
        when(itemMapper.toEntity(inputDto)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(resultDto);

        // When
        ItemDto result = itemService.createItem(inputDto);

        // Then
        assertNotNull(result);
        assertEquals("Test Item", result.name());
        verify(itemRepository).findByName(inputDto.name());
        verify(itemMapper).toEntity(inputDto);
        verify(itemRepository).persist(item);
        verify(itemMapper).toDto(item);
    }

    @Test
    void createItem_WhenItemAlreadyExists_ShouldThrowException() {
        // Given
        ItemDto inputDto = createItemDto();
        Item existingItem = createItem();
        
        when(itemRepository.findByName(inputDto.name())).thenReturn(Optional.of(existingItem));

        // When & Then
        assertThrows(ItemAlreadyExistsException.class, () -> itemService.createItem(inputDto));
        verify(itemRepository).findByName(inputDto.name());
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }

    @Test
    void createItem_WithZeroQuantityAndActiveStatus_ShouldThrowException() {
        // Given
        ItemDto inputDto = new ItemDto(null, "Test Item", "Description", List.of("url1"), BigDecimal.valueOf(19.99), ItemStatus.ACTIVE, 0);
        Item item = createItemWithQuantity(0);
        
        when(itemRepository.findByName(inputDto.name())).thenReturn(Optional.empty());
        when(itemMapper.toEntity(inputDto)).thenReturn(item);

        // When & Then
        assertThrows(InvalidItemStatusException.class, () -> itemService.createItem(inputDto));
    }

    @Test
    void createItem_WithPositiveQuantityAndOutOfStockStatus_ShouldThrowException() {
        // Given
        ItemDto inputDto = new ItemDto(null, "Test Item", "Description", List.of("url1"), BigDecimal.valueOf(19.99), ItemStatus.OUT_OF_STOCK, 5);
        Item item = createItemWithQuantity(5);
        
        when(itemRepository.findByName(inputDto.name())).thenReturn(Optional.empty());
        when(itemMapper.toEntity(inputDto)).thenReturn(item);

        // When & Then
        assertThrows(InvalidItemStatusException.class, () -> itemService.createItem(inputDto));
    }

    @Test
    void updateItem_ShouldUpdateAndReturnItem() {
        // Given
        UUID id = UUID.randomUUID();
        ItemDto inputDto = createItemDto();
        Item item = createItem();
        ItemDto resultDto = createItemDto();
        
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(itemRepository.findByName(inputDto.name())).thenReturn(Optional.empty());
        when(itemMapper.toDto(item)).thenReturn(resultDto);

        // When
        ItemDto result = itemService.updateItem(id, inputDto);

        // Then
        assertNotNull(result);
        verify(itemRepository).findById(id);
        verify(itemRepository).findByName(inputDto.name());
        verify(itemMapper).toDto(item);
    }

    @Test
    void updateItem_WhenItemNotExists_ShouldThrowException() {
        // Given
        UUID id = UUID.randomUUID();
        ItemDto inputDto = createItemDto();
        
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ItemNotFoundException.class, () -> itemService.updateItem(id, inputDto));
        verify(itemRepository).findById(id);
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }

    @Test
    void updateItem_WhenNameAlreadyTaken_ShouldThrowException() {
        // Given
        UUID id = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        ItemDto inputDto = createItemDto();
        Item item = createItem();
        Item existingItem = createItem();
        existingItem.setUuid(otherId);
        
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(itemRepository.findByName(inputDto.name())).thenReturn(Optional.of(existingItem));

        // When & Then
        assertThrows(ItemAlreadyExistsException.class, () -> itemService.updateItem(id, inputDto));
    }

    @Test
    void decreaseQuantity_ShouldDecreaseAndReturnItem() {
        // Given
        UUID id = UUID.randomUUID();
        Item item = createItemWithQuantity(10);
        ItemDto resultDto = createItemDto();
        
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(resultDto);

        // When
        ItemDto result = itemService.decreaseQuantity(id, 3);

        // Then
        assertNotNull(result);
        assertEquals(7, item.getQuantity());
        verify(itemRepository).findById(id);
        verify(itemMapper).toDto(item);
    }

    @Test
    void decreaseQuantity_WhenQuantityBecomesZero_ShouldSetOutOfStock() {
        // Given
        UUID id = UUID.randomUUID();
        Item item = createItemWithQuantity(5);
        item.setStatus(ItemStatus.ACTIVE);
        ItemDto resultDto = createItemDto();
        
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(resultDto);

        // When
        itemService.decreaseQuantity(id, 5);

        // Then
        assertEquals(0, item.getQuantity());
        assertEquals(ItemStatus.OUT_OF_STOCK, item.getStatus());
    }

    @Test
    void decreaseQuantity_WhenItemNotExists_ShouldThrowException() {
        // Given
        UUID id = UUID.randomUUID();
        
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ItemNotFoundException.class, () -> itemService.decreaseQuantity(id, 1));
        verify(itemRepository).findById(id);
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }

    private Item createItem() {
        Item item = new Item();
        item.setUuid(UUID.randomUUID());
        item.setName("Test Item");
        item.setPrice(BigDecimal.valueOf(19.99));
        item.setQuantity(10);
        item.setStatus(ItemStatus.ACTIVE);
        return item;
    }

    private ItemDto createItemDto() {
        return new ItemDto(UUID.randomUUID(),
                "Test Item",
                "Description",
                List.of("url1"),
                BigDecimal.valueOf(19.99),
                ItemStatus.ACTIVE,
                10);
    }

    private Item createItemWithQuantity(int quantity) {
        Item item = createItem();
        item.setQuantity(quantity);
        return item;
    }
}