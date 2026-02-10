package com.everage.eshop.service;

import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.dto.mapper.ItemMapper;
import com.everage.eshop.entity.Collection;
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
import java.time.LocalDateTime;
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
        UUID uuid = UUID.randomUUID();
        Item item = createItem();
        ItemDto itemDto = createItemDto();

        when(itemRepository.findById(uuid)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        // When
        ItemDto result = itemService.getItemById(uuid);

        // Then
        assertNotNull(result);
        assertEquals("Test Item", result.name());
        assertEquals("red", result.colour());
        assertNotNull(result.collection());
        verify(itemRepository).findById(uuid);
        verify(itemMapper).toDto(item);
    }

    @Test
    void getItemById_WhenItemNotExists_ShouldThrowException() {
        // Given
        UUID uuid = UUID.randomUUID();
        when(itemRepository.findById(uuid)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ItemNotFoundException.class, () -> itemService.getItemById(uuid));
        verify(itemRepository).findById(uuid);
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
        assertEquals("red", result.colour());
        assertNotNull(result.collection());
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
        ItemDto inputDto = new ItemDto(
                null, "Test Item", "Description",
                List.of("url1"), BigDecimal.valueOf(19.99),
                ItemStatus.ACTIVE, 0, null, null
        );
        Item item = createItemWithQuantity(0);

        when(itemRepository.findByName(inputDto.name())).thenReturn(Optional.empty());
        when(itemMapper.toEntity(inputDto)).thenReturn(item);

        // When & Then
        assertThrows(InvalidItemStatusException.class, () -> itemService.createItem(inputDto));
    }

    @Test
    void createItem_WithPositiveQuantityAndOutOfStockStatus_ShouldThrowException() {
        // Given
        ItemDto inputDto = new ItemDto(
                null, "Test Item", "Description",
                List.of("url1"), BigDecimal.valueOf(19.99),
                ItemStatus.OUT_OF_STOCK, 5, null, null
        );
        Item item = createItemWithQuantity(5);

        when(itemRepository.findByName(inputDto.name())).thenReturn(Optional.empty());
        when(itemMapper.toEntity(inputDto)).thenReturn(item);

        // When & Then
        assertThrows(InvalidItemStatusException.class, () -> itemService.createItem(inputDto));
    }

    @Test
    void updateItem_ShouldUpdateAndReturnItem() {
        // Given
        UUID uuid = UUID.randomUUID();
        ItemDto inputDto = createItemDto();
        Item item = createItem();
        ItemDto resultDto = createItemDto();

        when(itemRepository.findById(uuid)).thenReturn(Optional.of(item));
        when(itemRepository.findByName(inputDto.name())).thenReturn(Optional.empty());
        when(itemMapper.toDto(item)).thenReturn(resultDto);

        // When
        ItemDto result = itemService.updateItem(uuid, inputDto);

        // Then
        assertNotNull(result);
        assertEquals("Test Item", result.name());
        assertEquals("red", result.colour());
        verify(itemRepository).findById(uuid);
        verify(itemRepository).findByName(inputDto.name());
        verify(itemMapper).toDto(item);
    }

    @Test
    void updateItem_WhenItemNotExists_ShouldThrowException() {
        // Given
        UUID uuid = UUID.randomUUID();
        ItemDto inputDto = createItemDto();

        when(itemRepository.findById(uuid)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ItemNotFoundException.class, () -> itemService.updateItem(uuid, inputDto));
        verify(itemRepository).findById(uuid);
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }

    @Test
    void updateItem_WhenNameAlreadyTaken_ShouldThrowException() {
        // Given
        UUID uuid = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        ItemDto inputDto = createItemDto();
        Item item = createItem();
        Item existingItem = createItem();
        existingItem.setUuid(otherId);

        when(itemRepository.findById(uuid)).thenReturn(Optional.of(item));
        when(itemRepository.findByName(inputDto.name())).thenReturn(Optional.of(existingItem));

        // When & Then
        assertThrows(ItemAlreadyExistsException.class, () -> itemService.updateItem(uuid, inputDto));
    }

    @Test
    void deleteItem_WhenItemExists_ShouldDeleteItem() {
        // Given
        UUID uuid = UUID.randomUUID();
        Item item = createItem();

        when(itemRepository.findById(uuid)).thenReturn(Optional.of(item));

        // When
        itemService.deleteItem(uuid);

        // Then
        verify(itemRepository).findById(uuid);
        verify(itemRepository).delete(item);
    }

    @Test
    void deleteItem_WhenItemBelongsToCollection_ShouldRemoveFromCollectionAndDelete() {
        // Given
        UUID uuid = UUID.randomUUID();
        Item item = createItem();
        Collection collection = createCollection();
        collection.addItem(item);

        when(itemRepository.findById(uuid)).thenReturn(Optional.of(item));

        // When
        itemService.deleteItem(uuid);

        // Then
        assertNull(item.getCollection());
        verify(itemRepository).findById(uuid);
        verify(itemRepository).delete(item);
    }

    @Test
    void deleteItem_WhenItemNotExists_ShouldThrowException() {
        // Given
        UUID uuid = UUID.randomUUID();

        when(itemRepository.findById(uuid)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ItemNotFoundException.class, () -> itemService.deleteItem(uuid));
        verify(itemRepository).findById(uuid);
        verify(itemRepository, never()).delete(any());
    }

    @Test
    void getItemsByCollectionUuid_ShouldReturnItems() {
        // Given
        UUID collectionUuid = UUID.randomUUID();
        List<Item> items = List.of(createItem());
        List<ItemDto> itemDtos = List.of(createItemDto());

        when(itemRepository.findByCollectionUuid(collectionUuid)).thenReturn(items);
        when(itemMapper.toDtoList(items)).thenReturn(itemDtos);

        // When
        List<ItemDto> result = itemService.getItemsByCollectionUuid(collectionUuid);

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Item", result.get(0).name());
        verify(itemRepository).findByCollectionUuid(collectionUuid);
        verify(itemMapper).toDtoList(items);
    }

    @Test
    void getItemsByCollectionUuid_WhenNoItems_ShouldReturnEmptyList() {
        // Given
        UUID collectionUuid = UUID.randomUUID();

        when(itemRepository.findByCollectionUuid(collectionUuid)).thenReturn(List.of());
        when(itemMapper.toDtoList(List.of())).thenReturn(List.of());

        // When
        List<ItemDto> result = itemService.getItemsByCollectionUuid(collectionUuid);

        // Then
        assertTrue(result.isEmpty());
        verify(itemRepository).findByCollectionUuid(collectionUuid);
    }

    @Test
    void decreaseQuantity_ShouldDecreaseAndReturnItem() {
        // Given
        UUID uuid = UUID.randomUUID();
        Item item = createItemWithQuantity(10);
        ItemDto resultDto = createItemDto();

        when(itemRepository.findById(uuid)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(resultDto);

        // When
        ItemDto result = itemService.decreaseQuantity(uuid, 3);

        // Then
        assertNotNull(result);
        assertEquals(7, item.getQuantity());
        verify(itemRepository).findById(uuid);
        verify(itemMapper).toDto(item);
    }

    @Test
    void decreaseQuantity_WhenQuantityBecomesZero_ShouldSetOutOfStock() {
        // Given
        UUID uuid = UUID.randomUUID();
        Item item = createItemWithQuantity(5);
        item.setStatus(ItemStatus.ACTIVE);
        ItemDto resultDto = createItemDto();

        when(itemRepository.findById(uuid)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(resultDto);

        // When
        itemService.decreaseQuantity(uuid, 5);

        // Then
        assertEquals(0, item.getQuantity());
        assertEquals(ItemStatus.OUT_OF_STOCK, item.getStatus());
    }

    @Test
    void decreaseQuantity_WhenItemNotExists_ShouldThrowException() {
        // Given
        UUID uuid = UUID.randomUUID();

        when(itemRepository.findById(uuid)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ItemNotFoundException.class, () -> itemService.decreaseQuantity(uuid, 1));
        verify(itemRepository).findById(uuid);
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }

    // ============================================
    // Helper methods
    // ============================================

    private Item createItem() {
        Item item = new Item();
        item.setUuid(UUID.randomUUID());
        item.setName("Test Item");
        item.setDescription("Description");
        item.setPrice(BigDecimal.valueOf(19.99));
        item.setQuantity(10);
        item.setStatus(ItemStatus.ACTIVE);
        item.setColour("red");
        item.setImageUrls(List.of("url1"));
        return item;
    }

    private Collection createCollection() {
        Collection collection = new Collection();
        collection.setUuid(UUID.randomUUID());
        collection.setName("Test Collection");
        collection.setDescription("Collection Description");
        collection.setImageUrls(List.of("colUrl1"));
        return collection;
    }

    private ItemDto createItemDto() {
        return new ItemDto(
                UUID.randomUUID(),
                "Test Item",
                "Description",
                List.of("url1"),
                BigDecimal.valueOf(19.99),
                ItemStatus.ACTIVE,
                10,
                "red",
                new CollectionDto(
                        UUID.randomUUID(),
                        "Test Collection",
                        "Collection Description",
                        List.of("colUrl1"),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );
    }

    private ItemDto createItemDtoWithoutCollection() {
        return new ItemDto(
                UUID.randomUUID(),
                "Test Item",
                "Description",
                List.of("url1"),
                BigDecimal.valueOf(19.99),
                ItemStatus.ACTIVE,
                10,
                null,
                null
        );
    }

    private Item createItemWithQuantity(int quantity) {
        Item item = createItem();
        item.setQuantity(quantity);
        return item;
    }
}