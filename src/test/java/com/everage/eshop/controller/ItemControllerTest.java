package com.everage.eshop.controller;

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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        UUID id = UUID.randomUUID();
        ItemDto item = createItemDto();
        when(itemService.getItemById(id)).thenReturn(item);

        // When & Then
        mockMvc.perform(get("/api/items/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void getItemById_WhenItemNotExists_ShouldReturn404() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(itemService.getItemById(id)).thenThrow(new ItemNotFoundException("Item not found"));

        // When & Then
        mockMvc.perform(get("/api/items/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void addItem_WithValidData_ShouldCreateItem() throws Exception {
        // Given
        ItemDto inputDto = new ItemDto(null, "New Item", "Description", List.of("url1"), BigDecimal.valueOf(29.99), ItemStatus.ACTIVE, 10);
        ItemDto resultDto = createItemDto();
        when(itemService.createItem(any(ItemDto.class))).thenReturn(resultDto);

        // When & Then
        mockMvc.perform(post("/api/items/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void addItem_WithDuplicateName_ShouldReturn409() throws Exception {
        // Given
        ItemDto inputDto = new ItemDto(null, "Existing Item", "Description", List.of("url1"), BigDecimal.valueOf(29.99), ItemStatus.ACTIVE, 10);
        when(itemService.createItem(any(ItemDto.class))).thenThrow(new ItemAlreadyExistsException("Item already exists"));

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
        ItemDto inputDto = new ItemDto(null, "New Item", "Description", List.of("url1"), BigDecimal.valueOf(29.99), ItemStatus.ACTIVE, 0);
        when(itemService.createItem(any(ItemDto.class))).thenThrow(new InvalidItemStatusException("Invalid status"));

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
        UUID id = UUID.randomUUID();
        ItemDto inputDto = new ItemDto(null, "Updated Item", "Updated Description", List.of("url1"), BigDecimal.valueOf(39.99), ItemStatus.ACTIVE, 15);
        ItemDto resultDto = createItemDto();
        when(itemService.updateItem(eq(id), any(ItemDto.class))).thenReturn(resultDto);

        // When & Then
        mockMvc.perform(put("/api/items/{id}/update", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void updateItem_WhenItemNotExists_ShouldReturn404() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        ItemDto inputDto = createItemDto();
        when(itemService.updateItem(eq(id), any(ItemDto.class))).thenThrow(new ItemNotFoundException("Item not found"));

        // When & Then
        mockMvc.perform(put("/api/items/{id}/update", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    private ItemDto createItemDto() {
        return new ItemDto(UUID.randomUUID(),
                "Test Item",
                "Test Description",
                List.of("url1"),
                BigDecimal.valueOf(19.99),
                ItemStatus.ACTIVE,
                10);
    }
}