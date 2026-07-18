package com.everage.eshop.controller;

import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.exception.collection.CollectionNotFoundException;
import com.everage.eshop.service.CollectionService;
import com.everage.eshop.config.TestSecurityConfig;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CollectionController.class)
@Import(TestSecurityConfig.class)
class CollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CollectionService collectionService;

    @Autowired
    private JsonMapper objectMapper;

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

    // ── helpers ───────────────────────────────────────────────────────────────

    private CollectionDto createCollectionDto() {
        return new CollectionDto(
                UUID.randomUUID(), "Alien", "Desc",
                List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
