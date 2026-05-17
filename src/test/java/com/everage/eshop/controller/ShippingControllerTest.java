package com.everage.eshop.controller;

import com.everage.eshop.dto.ShippingRequest;
import com.everage.eshop.dto.ShippingResponse;
import com.everage.eshop.entity.ShippingProvider;
import com.everage.eshop.entity.ShippingStatus;
import com.everage.eshop.service.shipping.ShippingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShippingController.class)
class ShippingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShippingService shippingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createShipping_ShouldReturn200WithTrackingNumber() throws Exception {
        UUID orderId = UUID.randomUUID();
        ShippingRequest request = new ShippingRequest(
                orderId, 
                ShippingProvider.ZASILKOVNA, 
                "Main St 1, Prague",
                BigDecimal.valueOf(12.00),
                "12345",
                "Zasilkovna Prague",
                "Central Square 1"
        );
        ShippingResponse response = createShippingResponse(orderId, ShippingStatus.PENDING);

        when(shippingService.createShipping(any(ShippingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/shipping/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.trackingNumber").value("TRACK-ABC123"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createShipping_WhenOrderNotFound_ShouldReturn500() throws Exception {
        UUID orderId = UUID.randomUUID();
        ShippingRequest request = new ShippingRequest(
                orderId, 
                ShippingProvider.ZASILKOVNA, 
                "Main St 1",
                BigDecimal.valueOf(12.00),
                null,
                null,
                null
        );

        when(shippingService.createShipping(any(ShippingRequest.class)))
                .thenThrow(new RuntimeException("Order not found: " + orderId));

        mockMvc.perform(post("/api/shipping/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getShippingByOrder_ShouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(shippingService.getShippingByOrderId(orderId))
                .thenReturn(createShippingResponse(orderId, ShippingStatus.PENDING));

        mockMvc.perform(get("/api/shipping/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderUuid").value(orderId.toString()))
                .andExpect(jsonPath("$.provider").value("ZASILKOVNA"));
    }

    @Test
    void getShippingByOrder_WhenNotFound_ShouldReturn500() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(shippingService.getShippingByOrderId(orderId))
                .thenThrow(new RuntimeException("Shipping not found for order: " + orderId));

        mockMvc.perform(get("/api/shipping/order/{orderId}", orderId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateShippingStatus_ShouldReturn200WithNewStatus() throws Exception {
        UUID shippingId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(shippingService.updateShippingStatus(eq(shippingId), eq(ShippingStatus.IN_TRANSIT)))
                .thenReturn(createShippingResponse(orderId, ShippingStatus.IN_TRANSIT));

        mockMvc.perform(patch("/api/shipping/{shippingId}/status", shippingId)
                        .param("status", "IN_TRANSIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));
    }

    @Test
    void updateShippingStatus_WhenNotFound_ShouldReturn500() throws Exception {
        UUID shippingId = UUID.randomUUID();
        when(shippingService.updateShippingStatus(eq(shippingId), any()))
                .thenThrow(new RuntimeException("Shipping not found: " + shippingId));

        mockMvc.perform(patch("/api/shipping/{shippingId}/status", shippingId)
                        .param("status", "IN_TRANSIT"))
                .andExpect(status().isInternalServerError());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ShippingResponse createShippingResponse(UUID orderId, ShippingStatus status) {
        return new ShippingResponse(
                UUID.randomUUID(), 
                orderId,
                ShippingProvider.ZASILKOVNA,
                BigDecimal.valueOf(12.00),
                "TRACK-ABC123", 
                status,
                LocalDateTime.now().plusDays(5),
                "12345",
                "Zasilkovna Prague",
                "Central Square 1",
                "ZAS-123456",
                "https://label.url",
                LocalDateTime.now()
        );
    }
}
