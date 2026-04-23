package com.everage.eshop.service;

import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.dto.ItemRequest;
import com.everage.eshop.dto.mapper.ItemMapper;
import com.everage.eshop.entity.Collection;
import com.everage.eshop.entity.Item;
import com.everage.eshop.entity.ItemStatus;
import com.everage.eshop.exception.item.InvalidItemStatusException;
import com.everage.eshop.exception.item.ItemAlreadyExistsException;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

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

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ItemService itemService;

    @Test
    void getAllItems_ShouldReturnAllItems() {
        List<Item> items = List.of(createItem());
        List<ItemDto> itemDtos = List.of(createItemDto());

        when(itemRepository.findAll()).thenReturn(items);
        when(itemMapper.toDtoList(items)).thenReturn(itemDtos);

        List<ItemDto> result = itemService.getAllItems();

        assertEquals(1, result.size());
        verify(itemRepository).findAll();
        verify(itemMapper).toDtoList(items);
    }

    @Test
    void getItemById_WhenItemExists_ShouldReturnItem() {
        UUID uuid = UUID.randomUUID();
        Item item = createItem();
        ItemDto itemDto = createItemDto();

        when(itemRepository.findById(uuid)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.getItemById(uuid);

        assertNotNull(result);
        assertEquals("Test Item", result.name());
        assertEquals("red", result.color());
        assertNotNull(result.collection());
        verify(itemRepository).findById(uuid);
        verify(itemMapper).toDto(item);
    }

    @Test
    void getItemById_WhenItemNotExists_ShouldThrowException() {
        UUID uuid = UUID.randomUUID();
        when(itemRepository.findById(uuid)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.getItemById(uuid));
        verify(itemRepository).findById(uuid);
        verifyNoInteractions(itemMapper);
    }

    @Test
    void createItem_ShouldCreateAndReturnItem() {
        ItemRequest request = createItemRequest(10, ItemStatus.ACTIVE);
        ItemDto resultDto = createItemDto();

        when(itemRepository.findByName(request.name())).thenReturn(Optional.empty());
        when(itemMapper.toDto(any(Item.class))).thenReturn(resultDto);

        ItemDto result = itemService.createItem(request, List.of());

        assertNotNull(result);
        assertEquals("Test Item", result.name());
        verify(itemRepository).findByName(request.name());
        verify(itemRepository).persist(any(Item.class));
        verifyNoInteractions(storageService);
    }

    @Test
    void createItem_WhenItemAlreadyExists_ShouldThrowException() {
        ItemRequest request = createItemRequest(10, ItemStatus.ACTIVE);
        when(itemRepository.findByName(request.name())).thenReturn(Optional.of(createItem()));

        assertThrows(ItemAlreadyExistsException.class, () -> itemService.createItem(request, List.of()));
        verify(itemRepository).findByName(request.name());
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }

    @Test
    void createItem_WithZeroQuantityAndActiveStatus_ShouldThrowException() {
        ItemRequest request = createItemRequest(0, ItemStatus.ACTIVE);
        when(itemRepository.findByName(request.name())).thenReturn(Optional.empty());

        assertThrows(InvalidItemStatusException.class, () -> itemService.createItem(request, List.of()));
    }

    @Test
    void createItem_WithPositiveQuantityAndOutOfStockStatus_ShouldThrowException() {
        ItemRequest request = createItemRequest(5, ItemStatus.OUT_OF_STOCK);
        when(itemRepository.findByName(request.name())).thenReturn(Optional.empty());

        assertThrows(InvalidItemStatusException.class, () -> itemService.createItem(request, List.of()));
    }

    @Test
    void updateItem_ShouldUpdateAndReturnItem() {
        UUID uuid = UUID.randomUUID();
        Item item = createItem();
        ItemRequest request = new ItemRequest(
                "Test Item", "Description", BigDecimal.valueOf(19.99),
                ItemStatus.ACTIVE, 10, "red", List.of("url1")
        );
        ItemDto resultDto = createItemDto();

        when(itemRepository.findById(uuid)).thenReturn(Optional.of(item));
        when(itemRepository.findByName(request.name())).thenReturn(Optional.empty());
        when(itemMapper.toDto(item)).thenReturn(resultDto);

        ItemDto result = itemService.updateItem(uuid, request, List.of());

        assertNotNull(result);
        assertEquals("Test Item", result.name());
        verify(itemRepository).findById(uuid);
        verify(itemRepository).findByName(request.name());
        verify(itemMapper).toDto(item);
    }

    @Test
    void updateItem_DeletesRemovedImages() {
        UUID uuid = UUID.randomUUID();
        Item item = createItem();
        item.setImageUrls(List.of("https://media.example.com/items/old.jpg", "https://media.example.com/items/keep.jpg"));

        // keep only one, drop the other
        ItemRequest request = new ItemRequest(
                "Test Item", "Description", BigDecimal.valueOf(19.99),
                ItemStatus.ACTIVE, 10, "red", List.of("https://media.example.com/items/keep.jpg")
        );

        when(itemRepository.findById(uuid)).thenReturn(Optional.of(item));
        when(itemRepository.findByName(request.name())).thenReturn(Optional.empty());
        when(itemMapper.toDto(item)).thenReturn(createItemDto());

        itemService.updateItem(uuid, request, List.of());

        verify(storageService).deleteAll(List.of("https://media.example.com/items/old.jpg"));
    }

    @Test
    void updateItem_WhenItemNotExists_ShouldThrowException() {
        UUID uuid = UUID.randomUUID();
        ItemRequest request = createItemRequest(10, ItemStatus.ACTIVE);
        when(itemRepository.findById(uuid)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.updateItem(uuid, request, List.of()));
        verify(itemRepository).findById(uuid);
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }

    @Test
    void updateItem_WhenNameAlreadyTaken_ShouldThrowException() {
        UUID uuid = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        ItemRequest request = createItemRequest(10, ItemStatus.ACTIVE);
        Item item = createItem();
        Item existingItem = createItem();
        existingItem.setUuid(otherId);

        when(itemRepository.findById(uuid)).thenReturn(Optional.of(item));
        when(itemRepository.findByName(request.name())).thenReturn(Optional.of(existingItem));

        assertThrows(ItemAlreadyExistsException.class, () -> itemService.updateItem(uuid, request, List.of()));
    }

    @Test
    void deleteItem_WhenItemExists_ShouldDeleteItem() {
        UUID uuid = UUID.randomUUID();
        Item item = createItem();

        when(itemRepository.findById(uuid)).thenReturn(Optional.of(item));

        itemService.deleteItem(uuid);

        verify(storageService).deleteAll(item.getImageUrls());
        verify(itemRepository).delete(item);
    }

    @Test
    void deleteItem_WhenItemBelongsToCollection_ShouldRemoveFromCollectionAndDelete() {
        UUID uuid = UUID.randomUUID();
        Item item = createItem();
        Collection collection = createCollection();
        collection.addItem(item);

        when(itemRepository.findById(uuid)).thenReturn(Optional.of(item));

        itemService.deleteItem(uuid);

        assertNull(item.getCollection());
        verify(storageService).deleteAll(item.getImageUrls());
        verify(itemRepository).delete(item);
    }

    @Test
    void deleteItem_WhenItemNotExists_ShouldThrowException() {
        UUID uuid = UUID.randomUUID();
        when(itemRepository.findById(uuid)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.deleteItem(uuid));
        verify(itemRepository).findById(uuid);
        verify(itemRepository, never()).delete(any());
    }

    @Test
    void getItemsByCollectionUuid_ShouldReturnItems() {
        UUID collectionUuid = UUID.randomUUID();
        List<Item> items = List.of(createItem());
        List<ItemDto> itemDtos = List.of(createItemDto());

        when(itemRepository.findByCollectionUuid(collectionUuid)).thenReturn(items);
        when(itemMapper.toDtoList(items)).thenReturn(itemDtos);

        List<ItemDto> result = itemService.getItemsByCollectionUuid(collectionUuid);

        assertEquals(1, result.size());
        assertEquals("Test Item", result.get(0).name());
        verify(itemRepository).findByCollectionUuid(collectionUuid);
        verify(itemMapper).toDtoList(items);
    }

    @Test
    void getItemsByCollectionUuid_WhenNoItems_ShouldReturnEmptyList() {
        UUID collectionUuid = UUID.randomUUID();

        when(itemRepository.findByCollectionUuid(collectionUuid)).thenReturn(List.of());
        when(itemMapper.toDtoList(List.of())).thenReturn(List.of());

        List<ItemDto> result = itemService.getItemsByCollectionUuid(collectionUuid);

        assertTrue(result.isEmpty());
        verify(itemRepository).findByCollectionUuid(collectionUuid);
    }

    @Test
    void decreaseQuantity_ShouldDecreaseAndReturnItem() {
        UUID uuid = UUID.randomUUID();
        Item item = createItemWithQuantity(10);
        ItemDto resultDto = createItemDto();

        when(itemRepository.findById(uuid)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(resultDto);

        ItemDto result = itemService.decreaseQuantity(uuid, 3);

        assertNotNull(result);
        assertEquals(7, item.getQuantity());
        verify(itemRepository).findById(uuid);
        verify(itemMapper).toDto(item);
    }

    @Test
    void decreaseQuantity_WhenQuantityBecomesZero_ShouldSetOutOfStock() {
        UUID uuid = UUID.randomUUID();
        Item item = createItemWithQuantity(5);
        item.setStatus(ItemStatus.ACTIVE);

        when(itemRepository.findById(uuid)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(createItemDto());

        itemService.decreaseQuantity(uuid, 5);

        assertEquals(0, item.getQuantity());
        assertEquals(ItemStatus.OUT_OF_STOCK, item.getStatus());
    }

    @Test
    void decreaseQuantity_WhenItemNotExists_ShouldThrowException() {
        UUID uuid = UUID.randomUUID();
        when(itemRepository.findById(uuid)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.decreaseQuantity(uuid, 1));
        verify(itemRepository).findById(uuid);
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }

    // ============================================
    // Helper methods
    // ============================================

    private ItemRequest createItemRequest(int quantity, ItemStatus status) {
        return new ItemRequest("Test Item", "Description", BigDecimal.valueOf(19.99),
                status, quantity, "red", List.of("url1"));
    }

    private Item createItem() {
        Item item = new Item();
        item.setUuid(UUID.randomUUID());
        item.setName("Test Item");
        item.setDescription("Description");
        item.setPrice(BigDecimal.valueOf(19.99));
        item.setQuantity(10);
        item.setStatus(ItemStatus.ACTIVE);
        item.setColor("red");
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
                        List.of(),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );
    }

    private Item createItemWithQuantity(int quantity) {
        Item item = createItem();
        item.setQuantity(quantity);
        return item;
    }
}
