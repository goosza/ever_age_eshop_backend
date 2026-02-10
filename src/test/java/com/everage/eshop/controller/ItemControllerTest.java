package com.everage.eshop.controller;

import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.exception.item.ItemAlreadyExistsException;
import com.everage.eshop.exception.item.InvalidItemStatusException;
import com.everage.eshop.entity.ItemStatus;
import com.everage.eshop.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllItems_ShouldReturnAllItems() throws Exception {
        // Given
        List<ItemDto> items = List.of(createItemDto());
        when(itemService.getAllItems()).thenReturn(items);

        // When & Then
        mockMvc.perform(get("/api/items/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void getItemById_WhenItemExists_ShouldReturnItem() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        ItemDto item = createItemDto();
        when(itemService.getItemById(uuid)).thenReturn(item);

        // When & Then
        mockMvc.perform(get("/api/items/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.colour").value("red"))
                .andExpect(jsonPath("$.collection.name").value("Test Collection"));
    }

    @Test
    void getItemById_WhenItemNotExists_ShouldReturn404() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        when(itemService.getItemById(uuid))
                .thenThrow(new ItemNotFoundException("Item not found with uuid: " + uuid));

        // When & Then
        mockMvc.perform(get("/api/items/{uuid}", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void addItem_WithValidData_ShouldCreateItem() throws Exception {
        // Given
        ItemDto inputDto = new ItemDto(
                null,
                "New Item",
                "Description",
                List.of("url1"),
                BigDecimal.valueOf(29.99),
                ItemStatus.ACTIVE,
                10,
                "black",
                null
        );
        ItemDto resultDto = createItemDto();
        when(itemService.createItem(any(ItemDto.class))).thenReturn(resultDto);

        // When & Then
        mockMvc.perform(post("/api/items/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())  // 201
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void addItem_WithDuplicateName_ShouldReturn409() throws Exception {
        // Given
        ItemDto inputDto = new ItemDto(
                null,
                "Existing Item",
                "Description",
                List.of("url1"),
                BigDecimal.valueOf(29.99),
                ItemStatus.ACTIVE,
                10,
                null,
                null
        );
        when(itemService.createItem(any(ItemDto.class)))
                .thenThrow(new ItemAlreadyExistsException("Item already exists"));

        // When & Then
        mockMvc.perform(post("/api/items/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void addItem_WithInvalidStatus_ShouldReturn400() throws Exception {
        // Given
        ItemDto inputDto = new ItemDto(
                null,
                "New Item",
                "Description",
                List.of("url1"),
                BigDecimal.valueOf(29.99),
                ItemStatus.ACTIVE,
                0,
                null,
                null
        );
        when(itemService.createItem(any(ItemDto.class)))
                .thenThrow(new InvalidItemStatusException("Invalid status"));

        // When & Then
        mockMvc.perform(post("/api/items/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void updateItem_WithValidData_ShouldUpdateItem() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        ItemDto inputDto = new ItemDto(
                null,
                "Updated Item",
                "Updated Description",
                List.of("url1"),
                BigDecimal.valueOf(39.99),
                ItemStatus.ACTIVE,
                15,
                "blue",
                null
        );
        ItemDto resultDto = createItemDto();
        when(itemService.updateItem(eq(uuid), any(ItemDto.class))).thenReturn(resultDto);

        // When & Then
        mockMvc.perform(put("/api/items/{uuid}/update", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void updateItem_WhenItemNotExists_ShouldReturn404() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        ItemDto inputDto = createItemDto();
        when(itemService.updateItem(eq(uuid), any(ItemDto.class)))
                .thenThrow(new ItemNotFoundException("Item not found with uuid: " + uuid));

        // When & Then
        mockMvc.perform(put("/api/items/{uuid}/update", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void updateItem_WithDuplicateName_ShouldReturn409() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        ItemDto inputDto = createItemDto();
        when(itemService.updateItem(eq(uuid), any(ItemDto.class)))
                .thenThrow(new ItemAlreadyExistsException("Name already taken"));

        // When & Then
        mockMvc.perform(put("/api/items/{uuid}/update", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void deleteItem_WhenItemExists_ShouldReturn204() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        doNothing().when(itemService).deleteItem(uuid);

        // When & Then
        mockMvc.perform(delete("/api/items/{uuid}/delete", uuid))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteItem_WhenItemNotExists_ShouldReturn404() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        doThrow(new ItemNotFoundException("Item not found with uuid: " + uuid))
                .when(itemService).deleteItem(uuid);

        // When & Then
        mockMvc.perform(delete("/api/items/{uuid}/delete", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getItemsByCollectionUuid_ShouldReturnItems() throws Exception {
        // Given
        UUID collectionUuid = UUID.randomUUID();
        List<ItemDto> items = List.of(createItemDto());
        when(itemService.getItemsByCollectionUuid(collectionUuid)).thenReturn(items);

        // When & Then
        mockMvc.perform(get("/api/items/collection/{collectionUuid}", collectionUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Item"))
                .andExpect(jsonPath("$[0].collection.name").value("Test Collection"));
    }

    @Test
    void getItemsByCollectionUuid_WhenNoItems_ShouldReturnEmptyList() throws Exception {
        // Given
        UUID collectionUuid = UUID.randomUUID();
        when(itemService.getItemsByCollectionUuid(collectionUuid)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/items/collection/{collectionUuid}", collectionUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ============================================
    // Helper methods
    // ============================================

    private ItemDto createItemDto() {
        return new ItemDto(
                UUID.randomUUID(),
                "Test Item",
                "Test Description",
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
}