package com.everage.eshop.controller;

import com.everage.eshop.dto.ShippingResponse;
import com.everage.eshop.entity.ShippingProvider;
import com.everage.eshop.entity.ShippingStatus;
import com.everage.eshop.service.shipping.ShippingService;
import com.everage.eshop.config.TestSecurityConfig;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShippingController.class)
@Import(TestSecurityConfig.class)
class ShippingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShippingService shippingService;

    @Autowired
    private JsonMapper objectMapper;

    @Test
    void trackShipment_ShouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(shippingService.getShippingByTracking("TRACK-ABC123"))
                .thenReturn(createShippingResponse(orderId, ShippingStatus.PENDING));

        mockMvc.perform(get("/api/shipping/track/{trackingNumber}", "TRACK-ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("ZASILKOVNA"));
    }

    @Test
    void trackShipment_WhenNotFound_ShouldReturn500() throws Exception {
        when(shippingService.getShippingByTracking("UNKNOWN"))
                .thenThrow(new RuntimeException("Shipping not found"));

        mockMvc.perform(get("/api/shipping/track/{trackingNumber}", "UNKNOWN"))
                .andExpect(status().isInternalServerError());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ShippingResponse createShippingResponse(UUID orderId, ShippingStatus status) {
        return new ShippingResponse(
                UUID.randomUUID(), orderId,
                ShippingProvider.ZASILKOVNA, BigDecimal.valueOf(12.00),
                "TRACK-ABC123", status, LocalDateTime.now().plusDays(5),
                null, null, null, "CZ",
                "12345", "Zasilkovna Prague", "Central Square 1",
                "ZAS-123456", "https://label.url", LocalDateTime.now()
        );
    }
}
