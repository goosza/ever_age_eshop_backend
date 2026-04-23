package com.everage.eshop.controller;

import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.dto.CollectionMultipartRequest;
import com.everage.eshop.exception.collection.CollectionAlreadyExistsException;
import com.everage.eshop.exception.collection.CollectionNotFoundException;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.service.CollectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CollectionController.class)
class CollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CollectionService collectionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllCollections_ShouldReturnList() throws Exception {
        when(collectionService.getAllCollections()).thenReturn(List.of(createCollectionDto()));

        mockMvc.perform(get("/api/collections/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Alien"));
    }

    @Test
    void getCollectionById_WhenExists_ShouldReturn200() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(collectionService.getCollectionByUuid(uuid)).thenReturn(createCollectionDto());

        mockMvc.perform(get("/api/collections/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alien"));
    }

    @Test
    void getCollectionById_WhenNotFound_ShouldReturn404() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(collectionService.getCollectionByUuid(uuid))
                .thenThrow(new CollectionNotFoundException("Not found"));

        mockMvc.perform(get("/api/collections/{uuid}", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createCollection_ShouldReturn201() throws Exception {
        CollectionMultipartRequest request = new CollectionMultipartRequest("Alien", "Desc", null);
        when(collectionService.createCollection(any(), any())).thenReturn(createCollectionDto());

        mockMvc.perform(multipart("/api/collections/add")
                        .file(jsonFile("collection", request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alien"));
    }

    @Test
    void createCollection_WhenNameExists_ShouldReturn409() throws Exception {
        CollectionMultipartRequest request = new CollectionMultipartRequest("Alien", "Desc", null);
        when(collectionService.createCollection(any(), any()))
                .thenThrow(new CollectionAlreadyExistsException("Already exists"));

        mockMvc.perform(multipart("/api/collections/add")
                        .file(jsonFile("collection", request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void updateCollection_ShouldReturn200() throws Exception {
        UUID uuid = UUID.randomUUID();
        CollectionMultipartRequest request = new CollectionMultipartRequest("Alien Updated", "Desc", List.of());
        when(collectionService.updateCollection(eq(uuid), any(), any())).thenReturn(createCollectionDto());

        mockMvc.perform(multipart(PUT, "/api/collections/{uuid}", uuid)
                        .file(jsonFile("collection", request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alien"));
    }

    @Test
    void updateCollection_WhenNotFound_ShouldReturn404() throws Exception {
        UUID uuid = UUID.randomUUID();
        CollectionMultipartRequest request = new CollectionMultipartRequest("X", null, null);
        when(collectionService.updateCollection(eq(uuid), any(), any()))
                .thenThrow(new CollectionNotFoundException("Not found"));

        mockMvc.perform(multipart(PUT, "/api/collections/{uuid}", uuid)
                        .file(jsonFile("collection", request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCollection_ShouldReturn204() throws Exception {
        UUID uuid = UUID.randomUUID();
        doNothing().when(collectionService).deleteCollection(uuid);

        mockMvc.perform(delete("/api/collections/{uuid}", uuid))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCollection_WhenNotFound_ShouldReturn404() throws Exception {
        UUID uuid = UUID.randomUUID();
        doThrow(new CollectionNotFoundException("Not found"))
                .when(collectionService).deleteCollection(uuid);

        mockMvc.perform(delete("/api/collections/{uuid}", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    void addItemToCollection_ShouldReturn200() throws Exception {
        UUID collectionUuid = UUID.randomUUID();
        UUID itemUuid = UUID.randomUUID();
        when(collectionService.addItemToCollection(collectionUuid, itemUuid))
                .thenReturn(createCollectionDto());

        mockMvc.perform(post("/api/collections/{collectionUuid}/items/{itemUuid}",
                        collectionUuid, itemUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alien"));
    }

    @Test
    void addItemToCollection_WhenItemNotFound_ShouldReturn404() throws Exception {
        UUID collectionUuid = UUID.randomUUID();
        UUID itemUuid = UUID.randomUUID();
        when(collectionService.addItemToCollection(collectionUuid, itemUuid))
                .thenThrow(new ItemNotFoundException("Item not found"));

        mockMvc.perform(post("/api/collections/{collectionUuid}/items/{itemUuid}",
                        collectionUuid, itemUuid))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeItemFromCollection_ShouldReturn200() throws Exception {
        UUID collectionUuid = UUID.randomUUID();
        UUID itemUuid = UUID.randomUUID();
        when(collectionService.removeItemFromCollection(collectionUuid, itemUuid))
                .thenReturn(createCollectionDto());

        mockMvc.perform(delete("/api/collections/{collectionUuid}/items/{itemUuid}/remove",
                        collectionUuid, itemUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alien"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MockMultipartFile jsonFile(String name, Object value) throws Exception {
        return new MockMultipartFile(
                name, "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(value)
        );
    }

    private CollectionDto createCollectionDto() {
        return new CollectionDto(
                UUID.randomUUID(), "Alien", "Desc",
                List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
