package com.everage.eshop.controller;

import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.exception.ItemNotFoundException;
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
        ItemDto inputDto = new ItemDto(null, "New Item", BigDecimal.valueOf(29.99));
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

    private ItemDto createItemDto() {
        return new ItemDto(UUID.randomUUID(), "Test Item", BigDecimal.valueOf(19.99));
    }
}