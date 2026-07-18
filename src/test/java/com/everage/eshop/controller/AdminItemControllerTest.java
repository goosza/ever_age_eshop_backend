package com.everage.eshop.controller;

import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.dto.ItemRequest;
import com.everage.eshop.entity.ItemStatus;
import com.everage.eshop.exception.item.InvalidItemStatusException;
import com.everage.eshop.exception.item.ItemAlreadyExistsException;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.service.ItemService;
import com.everage.eshop.config.TestSecurityConfig;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminItemController.class)
@Import(TestSecurityConfig.class)
class AdminItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @Autowired
    private JsonMapper objectMapper;

    @Test
    void createItem_WithValidData_ShouldReturn201() throws Exception {
        ItemRequest request = new ItemRequest(
                "New Item", "Description", BigDecimal.valueOf(29.99),
                BigDecimal.valueOf(0.500), ItemStatus.ACTIVE, 10, "black", null
        );
        when(itemService.createItem(any(ItemRequest.class), any())).thenReturn(createItemDto());

        mockMvc.perform(multipart("/api/admin/items")
                        .file(jsonFile("item", request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void createItem_WithDuplicateName_ShouldReturn409() throws Exception {
        ItemRequest request = new ItemRequest(
                "Existing Item", "Description", BigDecimal.valueOf(29.99),
                BigDecimal.valueOf(0.500), ItemStatus.ACTIVE, 10, null, null
        );
        when(itemService.createItem(any(ItemRequest.class), any()))
                .thenThrow(new ItemAlreadyExistsException("Item already exists"));

        mockMvc.perform(multipart("/api/admin/items")
                        .file(jsonFile("item", request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createItem_WithInvalidStatus_ShouldReturn400() throws Exception {
        ItemRequest request = new ItemRequest(
                "New Item", "Description", BigDecimal.valueOf(29.99),
                BigDecimal.valueOf(0.500), ItemStatus.ACTIVE, 0, null, null
        );
        when(itemService.createItem(any(ItemRequest.class), any()))
                .thenThrow(new InvalidItemStatusException("Invalid status"));

        mockMvc.perform(multipart("/api/admin/items")
                        .file(jsonFile("item", request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateItem_WithValidData_ShouldReturn200() throws Exception {
        UUID uuid = UUID.randomUUID();
        ItemRequest request = new ItemRequest(
                "Updated Item", "Updated Description", BigDecimal.valueOf(39.99),
                BigDecimal.valueOf(0.500), ItemStatus.ACTIVE, 15, "blue", List.of("url1")
        );
        when(itemService.updateItem(eq(uuid), any(ItemRequest.class), any())).thenReturn(createItemDto());

        mockMvc.perform(multipart(PUT, "/api/admin/items/{uuid}", uuid)
                        .file(jsonFile("item", request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void updateItem_WhenNotFound_ShouldReturn404() throws Exception {
        UUID uuid = UUID.randomUUID();
        ItemRequest request = new ItemRequest(
                "Test Item", "Description", BigDecimal.valueOf(19.99),
                BigDecimal.valueOf(0.500), ItemStatus.ACTIVE, 10, "red", List.of("url1")
        );
        when(itemService.updateItem(eq(uuid), any(ItemRequest.class), any()))
                .thenThrow(new ItemNotFoundException("Item not found with uuid: " + uuid));

        mockMvc.perform(multipart(PUT, "/api/admin/items/{uuid}", uuid)
                        .file(jsonFile("item", request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateQuantity_ShouldReturn200() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(itemService.updateQuantity(uuid, 10)).thenReturn(createItemDto());

        mockMvc.perform(patch("/api/admin/items/{uuid}/quantity", uuid)
                        .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void deleteItem_ShouldReturn204() throws Exception {
        UUID uuid = UUID.randomUUID();
        doNothing().when(itemService).deleteItem(uuid);

        mockMvc.perform(delete("/api/admin/items/{uuid}", uuid))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteItem_WhenNotFound_ShouldReturn404() throws Exception {
        UUID uuid = UUID.randomUUID();
        doThrow(new ItemNotFoundException("Item not found with uuid: " + uuid))
                .when(itemService).deleteItem(uuid);

        mockMvc.perform(delete("/api/admin/items/{uuid}", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MockMultipartFile jsonFile(String name, Object value) throws Exception {
        return new MockMultipartFile(
                name, "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(value)
        );
    }

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
