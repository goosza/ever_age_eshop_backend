package com.everage.eshop.service;

import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.dto.CollectionMultipartRequest;
import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.dto.mapper.CollectionMapper;
import com.everage.eshop.dto.mapper.ItemMapper;
import com.everage.eshop.entity.Collection;
import com.everage.eshop.entity.Item;
import com.everage.eshop.entity.ItemStatus;
import com.everage.eshop.exception.collection.CollectionAlreadyExistsException;
import com.everage.eshop.exception.collection.CollectionNotFoundException;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.exception.item.ItemNotInCollectionException;
import com.everage.eshop.repository.CollectionRepository;
import com.everage.eshop.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CollectionMapper collectionMapper;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private CollectionService collectionService;

    // ── getAllCollections ─────────────────────────────────────────────────────

    @Test
    void getAllCollections_ShouldReturnAll() {
        List<Collection> collections = List.of(createCollection());
        List<CollectionDto> dtos = List.of(createCollectionDto());

        when(collectionRepository.findAll()).thenReturn(collections);
        when(collectionMapper.toDtoList(collections)).thenReturn(dtos);
        when(storageService.toPublicUrls(any())).thenAnswer(inv -> inv.getArgument(0));

        List<CollectionDto> result = collectionService.getAllCollections();

        assertEquals(1, result.size());
        verify(collectionRepository).findAll();
    }

    // ── getCollectionByUuid ───────────────────────────────────────────────────

    @Test
    void getCollectionByUuid_WhenExists_ShouldReturnWithItems() {
        UUID uuid = UUID.randomUUID();
        Collection collection = createCollection();
        CollectionDto dto = createCollectionDto();

        when(collectionRepository.findByUuid(uuid)).thenReturn(Optional.of(collection));
        when(collectionMapper.toDto(collection)).thenReturn(dto);
        when(itemMapper.toDtoListWithoutCollection(collection.getItems())).thenReturn(List.of());
        when(storageService.toPublicUrls(any())).thenAnswer(inv -> inv.getArgument(0));

        CollectionDto result = collectionService.getCollectionByUuid(uuid);

        assertNotNull(result);
        verify(collectionRepository).findByUuid(uuid);
    }

    @Test
    void getCollectionByUuid_WhenNotFound_ShouldThrow() {
        UUID uuid = UUID.randomUUID();
        when(collectionRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(CollectionNotFoundException.class, () -> collectionService.getCollectionByUuid(uuid));
    }

    // ── createCollection ──────────────────────────────────────────────────────

    @Test
    void createCollection_ShouldPersistAndReturn() {
        CollectionMultipartRequest request = new CollectionMultipartRequest("Alien", "Desc", null);
        Collection collection = createCollection();
        CollectionDto dto = createCollectionDto();

        when(collectionRepository.findByName("Alien")).thenReturn(Optional.empty());
        when(collectionRepository.persist(any(Collection.class))).thenReturn(collection);
        when(collectionMapper.toDto(collection)).thenReturn(dto);
        when(storageService.toPublicUrls(any())).thenAnswer(inv -> inv.getArgument(0));

        CollectionDto result = collectionService.createCollection(request, List.of());

        assertNotNull(result);
        verify(collectionRepository).persist(any(Collection.class));
    }

    @Test
    void createCollection_WhenNameExists_ShouldThrow() {
        CollectionMultipartRequest request = new CollectionMultipartRequest("Alien", "Desc", null);
        when(collectionRepository.findByName("Alien")).thenReturn(Optional.of(createCollection()));

        assertThrows(CollectionAlreadyExistsException.class,
                () -> collectionService.createCollection(request, List.of()));
        verify(collectionRepository, never()).persist(any());
    }

    // ── updateCollection ──────────────────────────────────────────────────────

    @Test
    void updateCollection_ShouldUpdateAndReturn() {
        UUID uuid = UUID.randomUUID();
        Collection collection = createCollection();
        collection.setImageUrls(new ArrayList<>(List.of("collections/old.jpg")));
        CollectionDto dto = createCollectionDto();

        CollectionMultipartRequest request = new CollectionMultipartRequest(
                "Alien", "New desc", List.of()
        );

        when(collectionRepository.findByUuid(uuid)).thenReturn(Optional.of(collection));
        when(collectionRepository.merge(collection)).thenReturn(collection);
        when(collectionMapper.toDto(collection)).thenReturn(dto);
        when(itemMapper.toDtoListWithoutCollection(any())).thenReturn(List.of());
        when(storageService.toPublicUrls(any())).thenAnswer(inv -> inv.getArgument(0));

        CollectionDto result = collectionService.updateCollection(uuid, request, List.of());

        assertNotNull(result);
        verify(storageService).deleteAll(List.of("collections/old.jpg"));
        verify(collectionRepository).merge(collection);
    }

    @Test
    void updateCollection_WhenNotFound_ShouldThrow() {
        UUID uuid = UUID.randomUUID();
        when(collectionRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(CollectionNotFoundException.class,
                () -> collectionService.updateCollection(uuid,
                        new CollectionMultipartRequest("X", null, null), List.of()));
    }

    @Test
    void updateCollection_WhenNewNameTaken_ShouldThrow() {
        UUID uuid = UUID.randomUUID();
        Collection collection = createCollection();
        collection.setName("OldName");
        Collection other = createCollection();
        other.setUuid(UUID.randomUUID());

        when(collectionRepository.findByUuid(uuid)).thenReturn(Optional.of(collection));
        when(collectionRepository.findByName("NewName")).thenReturn(Optional.of(other));

        assertThrows(CollectionAlreadyExistsException.class,
                () -> collectionService.updateCollection(uuid,
                        new CollectionMultipartRequest("NewName", null, null), List.of()));
    }

    // ── deleteCollection ──────────────────────────────────────────────────────

    @Test
    void deleteCollection_ShouldDeleteImagesAndCollection() {
        UUID uuid = UUID.randomUUID();
        Collection collection = createCollection();
        collection.setImageUrls(new ArrayList<>(List.of("collections/img.jpg")));

        when(collectionRepository.findByUuid(uuid)).thenReturn(Optional.of(collection));

        collectionService.deleteCollection(uuid);

        verify(storageService).deleteAll(List.of("collections/img.jpg"));
        verify(collectionRepository).delete(collection);
    }

    @Test
    void deleteCollection_WhenNotFound_ShouldThrow() {
        UUID uuid = UUID.randomUUID();
        when(collectionRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(CollectionNotFoundException.class, () -> collectionService.deleteCollection(uuid));
        verify(collectionRepository, never()).delete(any());
    }

    // ── addItemToCollection ───────────────────────────────────────────────────

    @Test
    void addItemToCollection_ShouldAddAndReturn() {
        UUID collectionUuid = UUID.randomUUID();
        UUID itemUuid = UUID.randomUUID();
        Collection collection = createCollection();
        Item item = createItem();
        CollectionDto dto = createCollectionDto();

        when(collectionRepository.findByUuid(collectionUuid)).thenReturn(Optional.of(collection));
        when(itemRepository.findByUuid(itemUuid)).thenReturn(Optional.of(item));
        when(collectionRepository.persist(collection)).thenReturn(collection);
        when(collectionMapper.toDto(collection)).thenReturn(dto);
        when(itemMapper.toDtoListWithoutCollection(any())).thenReturn(List.of());
        when(storageService.toPublicUrls(any())).thenAnswer(inv -> inv.getArgument(0));

        CollectionDto result = collectionService.addItemToCollection(collectionUuid, itemUuid);

        assertNotNull(result);
        assertTrue(collection.getItems().contains(item));
        verify(collectionRepository).persist(collection);
    }

    @Test
    void addItemToCollection_WhenCollectionNotFound_ShouldThrow() {
        UUID collectionUuid = UUID.randomUUID();
        UUID itemUuid = UUID.randomUUID();
        when(collectionRepository.findByUuid(collectionUuid)).thenReturn(Optional.empty());

        assertThrows(CollectionNotFoundException.class,
                () -> collectionService.addItemToCollection(collectionUuid, itemUuid));
    }

    @Test
    void addItemToCollection_WhenItemNotFound_ShouldThrow() {
        UUID collectionUuid = UUID.randomUUID();
        UUID itemUuid = UUID.randomUUID();
        when(collectionRepository.findByUuid(collectionUuid)).thenReturn(Optional.of(createCollection()));
        when(itemRepository.findByUuid(itemUuid)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,
                () -> collectionService.addItemToCollection(collectionUuid, itemUuid));
    }

    // ── removeItemFromCollection ──────────────────────────────────────────────

    @Test
    void removeItemFromCollection_ShouldRemoveAndReturn() {
        UUID collectionUuid = UUID.randomUUID();
        UUID itemUuid = UUID.randomUUID();
        Collection collection = createCollection();
        Item item = createItem();
        collection.addItem(item);
        CollectionDto dto = createCollectionDto();

        when(collectionRepository.findByUuid(collectionUuid)).thenReturn(Optional.of(collection));
        when(itemRepository.findByUuid(itemUuid)).thenReturn(Optional.of(item));
        when(collectionRepository.persist(collection)).thenReturn(collection);
        when(collectionMapper.toDto(collection)).thenReturn(dto);
        when(itemMapper.toDtoListWithoutCollection(any())).thenReturn(List.of());
        when(storageService.toPublicUrls(any())).thenAnswer(inv -> inv.getArgument(0));

        CollectionDto result = collectionService.removeItemFromCollection(collectionUuid, itemUuid);

        assertNotNull(result);
        assertFalse(collection.getItems().contains(item));
    }

    @Test
    void removeItemFromCollection_WhenItemNotInCollection_ShouldThrow() {
        UUID collectionUuid = UUID.randomUUID();
        UUID itemUuid = UUID.randomUUID();
        Collection collection = createCollection();
        Item item = createItem();

        when(collectionRepository.findByUuid(collectionUuid)).thenReturn(Optional.of(collection));
        when(itemRepository.findByUuid(itemUuid)).thenReturn(Optional.of(item));

        assertThrows(ItemNotInCollectionException.class,
                () -> collectionService.removeItemFromCollection(collectionUuid, itemUuid));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Collection createCollection() {
        Collection c = new Collection();
        c.setUuid(UUID.randomUUID());
        c.setName("Alien");
        c.setDescription("Desc");
        c.setImageUrls(new ArrayList<>());
        c.setItems(new ArrayList<>());
        return c;
    }

    private Item createItem() {
        Item item = new Item();
        item.setUuid(UUID.randomUUID());
        item.setName("Test Item");
        item.setPrice(BigDecimal.valueOf(99.99));
        item.setWeight(BigDecimal.valueOf(0.500));
        item.setQuantity(10);
        item.setStatus(ItemStatus.ACTIVE);
        item.setImageUrls(new ArrayList<>());
        return item;
    }

    private CollectionDto createCollectionDto() {
        return new CollectionDto(
                UUID.randomUUID(), "Alien", "Desc",
                List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private ItemDto createItemDto() {
        return new ItemDto(
                UUID.randomUUID(), "Test Item", "Desc",
                List.of(), BigDecimal.valueOf(99.99), BigDecimal.valueOf(0.500),
                ItemStatus.ACTIVE, 10, null, null
        );
    }
}
