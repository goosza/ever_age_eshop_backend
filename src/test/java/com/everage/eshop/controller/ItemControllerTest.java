package com.everage.eshop.controller;

import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.entity.ItemStatus;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.service.ItemService;
import com.everage.eshop.config.TestSecurityConfig;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@Import(TestSecurityConfig.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @Autowired
    private JsonMapper objectMapper;

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
    void getItemsPaged_ShouldReturnPageOfItems() throws Exception {
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("name").ascending());
        var page = new PageImpl<>(List.of(createItemDto()), pageRequest, 1);
        when(itemService.getAllItems(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].name").value("Test Item"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getItemsPaged_WithPageAndSizeParams_ShouldReturnPage() throws Exception {
        var page = new PageImpl<ItemDto>(List.of(), PageRequest.of(2, 5), 0);
        when(itemService.getAllItems(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/items?page=2&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
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
    void getItemsByCollectionUuid_ShouldReturnItems() throws Exception {
        UUID collectionUuid = UUID.randomUUID();
        when(itemService.getItemsByCollectionUuid(collectionUuid)).thenReturn(List.of(createItemDto()));

        mockMvc.perform(get("/api/items/collection/{collectionUuid}", collectionUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Item"));
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

    // ── helpers ───────────────────────────────────────────────────────────────

    private ItemDto createItemDto() {
        return new ItemDto(
                UUID.randomUUID(), "Test Item", "Test Description",
                List.of("url1"), BigDecimal.valueOf(19.99), BigDecimal.valueOf(0.500),
                ItemStatus.ACTIVE, 10, "red",
                new CollectionDto(UUID.randomUUID(), "Test Collection", "Collection Description",
                        List.of("colUrl1"), List.of(), LocalDateTime.now(), LocalDateTime.now())
        );
    }
}
