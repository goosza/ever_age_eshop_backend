package com.everage.eshop.controller;

import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.dto.ItemRequest;
import com.everage.eshop.entity.ItemStatus;
import com.everage.eshop.exception.item.InvalidItemStatusException;
import com.everage.eshop.exception.item.ItemAlreadyExistsException;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.HttpMethod.PUT;

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
        when(itemService.getAllItems()).thenReturn(List.of(createItemDto()));

        mockMvc.perform(get("/api/items/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void getItemById_WhenItemExists_ShouldReturnItem() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(itemService.getItemById(uuid)).thenReturn(createItemDto());

        mockMvc.perform(get("/api/items/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.color").value("red"))
                .andExpect(jsonPath("$.collection.name").value("Test Collection"));
    }

    @Test
    void getItemById_WhenItemNotExists_ShouldReturn404() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(itemService.getItemById(uuid))
                .thenThrow(new ItemNotFoundException("Item not found with uuid: " + uuid));

        mockMvc.perform(get("/api/items/{uuid}", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void addItem_WithValidData_ShouldCreateItem() throws Exception {
        ItemRequest request = new ItemRequest(
                "New Item", "Description", BigDecimal.valueOf(29.99),
                ItemStatus.ACTIVE, 10, "black", null
        );
        when(itemService.createItem(any(ItemRequest.class), any())).thenReturn(createItemDto());

        mockMvc.perform(multipart("/api/items/add")
                        .file(jsonFile("item", request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void addItem_WithDuplicateName_ShouldReturn409() throws Exception {
        ItemRequest request = new ItemRequest(
                "Existing Item", "Description", BigDecimal.valueOf(29.99),
                ItemStatus.ACTIVE, 10, null, null
        );
        when(itemService.createItem(any(ItemRequest.class), any()))
                .thenThrow(new ItemAlreadyExistsException("Item already exists"));

        mockMvc.perform(multipart("/api/items/add")
                        .file(jsonFile("item", request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void addItem_WithInvalidStatus_ShouldReturn400() throws Exception {
        ItemRequest request = new ItemRequest(
                "New Item", "Description", BigDecimal.valueOf(29.99),
                ItemStatus.ACTIVE, 0, null, null
        );
        when(itemService.createItem(any(ItemRequest.class), any()))
                .thenThrow(new InvalidItemStatusException("Invalid status"));

        mockMvc.perform(multipart("/api/items/add")
                        .file(jsonFile("item", request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateItem_WithValidData_ShouldUpdateItem() throws Exception {
        UUID uuid = UUID.randomUUID();
        ItemRequest request = new ItemRequest(
                "Updated Item", "Updated Description", BigDecimal.valueOf(39.99),
                ItemStatus.ACTIVE, 15, "blue", List.of("url1")
        );
        when(itemService.updateItem(eq(uuid), any(ItemRequest.class), any())).thenReturn(createItemDto());

        mockMvc.perform(multipart(PUT, "/api/items/{uuid}/update", uuid)
                        .file(jsonFile("item", request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void updateItem_WhenItemNotExists_ShouldReturn404() throws Exception {
        UUID uuid = UUID.randomUUID();
        ItemRequest request = new ItemRequest(
                "Test Item", "Description", BigDecimal.valueOf(19.99),
                ItemStatus.ACTIVE, 10, "red", List.of("url1")
        );
        when(itemService.updateItem(eq(uuid), any(ItemRequest.class), any()))
                .thenThrow(new ItemNotFoundException("Item not found with uuid: " + uuid));

        mockMvc.perform(multipart(PUT, "/api/items/{uuid}/update", uuid)
                        .file(jsonFile("item", request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateItem_WithDuplicateName_ShouldReturn409() throws Exception {
        UUID uuid = UUID.randomUUID();
        ItemRequest request = new ItemRequest(
                "Test Item", "Description", BigDecimal.valueOf(19.99),
                ItemStatus.ACTIVE, 10, "red", List.of("url1")
        );
        when(itemService.updateItem(eq(uuid), any(ItemRequest.class), any()))
                .thenThrow(new ItemAlreadyExistsException("Name already taken"));

        mockMvc.perform(multipart(PUT, "/api/items/{uuid}/update", uuid)
                        .file(jsonFile("item", request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deleteItem_WhenItemExists_ShouldReturn204() throws Exception {
        UUID uuid = UUID.randomUUID();
        doNothing().when(itemService).deleteItem(uuid);

        mockMvc.perform(delete("/api/items/{uuid}/delete", uuid))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteItem_WhenItemNotExists_ShouldReturn404() throws Exception {
        UUID uuid = UUID.randomUUID();
        doThrow(new ItemNotFoundException("Item not found with uuid: " + uuid))
                .when(itemService).deleteItem(uuid);

        mockMvc.perform(delete("/api/items/{uuid}/delete", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getItemsByCollectionUuid_ShouldReturnItems() throws Exception {
        UUID collectionUuid = UUID.randomUUID();
        when(itemService.getItemsByCollectionUuid(collectionUuid)).thenReturn(List.of(createItemDto()));

        mockMvc.perform(get("/api/items/collection/{collectionUuid}", collectionUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Item"))
                .andExpect(jsonPath("$[0].collection.name").value("Test Collection"));
    }

    @Test
    void getItemsByCollectionUuid_WhenNoItems_ShouldReturnEmptyList() throws Exception {
        UUID collectionUuid = UUID.randomUUID();
        when(itemService.getItemsByCollectionUuid(collectionUuid)).thenReturn(List.of());

        mockMvc.perform(get("/api/items/collection/{collectionUuid}", collectionUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ============================================
    // Helper methods
    // ============================================

    private MockMultipartFile jsonFile(String name, Object value) throws Exception {
        return new MockMultipartFile(
                name, "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(value)
        );
    }

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
                        List.of(),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );
    }
}
