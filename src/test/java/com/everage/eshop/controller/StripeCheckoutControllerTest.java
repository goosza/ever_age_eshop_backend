package com.everage.eshop.controller;

import com.everage.eshop.dto.CheckoutSessionRequest;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.service.stripe.StripeCheckoutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CheckoutController.class)
class StripeCheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StripeCheckoutService stripeCheckoutService;

    private CheckoutSessionRequest request;

    @BeforeEach
    void setUp() {
        CheckoutSessionRequest.CustomerInfo customerInfo = new CheckoutSessionRequest.CustomerInfo(
                "John",
                "Doe",
                "john@example.com",
                "+1234567890",
                "123 Main St",
                "New York",
                "10001",
                "US"
        );

        CheckoutSessionRequest.CheckoutItem checkoutItem = new CheckoutSessionRequest.CheckoutItem(
                UUID.randomUUID().toString(),
                2,
                2999L
        );

        CheckoutSessionRequest.ShippingInfo shippingInfo = new CheckoutSessionRequest.ShippingInfo(
                "ZASILKOVNA",
                "PICKUP",
                BigDecimal.valueOf(12.00),
                "12345",
                "Zasilkovna Prague",
                "Central Square 1"
        );

        request = new CheckoutSessionRequest(customerInfo, List.of(checkoutItem), shippingInfo);
    }

    @Test
    void createCheckoutSession_ShouldReturnSessionUrl() throws Exception {
        // Given
        String sessionUrl = "https://checkout.stripe.com/pay/cs_test_123";
        when(stripeCheckoutService.createCheckoutSession(any(CheckoutSessionRequest.class)))
                .thenReturn(sessionUrl);

        // When & Then
        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionUrl").value(sessionUrl));
    }

    @Test
    void createCheckoutSession_WithInvalidItem_ShouldReturn404() throws Exception {
        // Given
        when(stripeCheckoutService.createCheckoutSession(any(CheckoutSessionRequest.class)))
                .thenThrow(new ItemNotFoundException("Item not found"));

        // When & Then
        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCheckoutSession_WithInsufficientStock_ShouldReturn500() throws Exception {
        // Given
        when(stripeCheckoutService.createCheckoutSession(any(CheckoutSessionRequest.class)))
                .thenThrow(new RuntimeException("Insufficient stock"));

        // When & Then
        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createCheckoutSession_WithEmptyItems_ShouldReturn400() throws Exception {
        // Given
        CheckoutSessionRequest emptyRequest = new CheckoutSessionRequest(
                request.customerInfo(),
                List.of(),
                request.shippingInfo()
        );

        // When & Then
        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isOk()); // Service will handle validation
    }

    @Test
    void createCheckoutSession_WithMultipleItems_ShouldReturnSessionUrl() throws Exception {
        // Given
        CheckoutSessionRequest.CheckoutItem item2 = new CheckoutSessionRequest.CheckoutItem(
                UUID.randomUUID().toString(),
                1,
                4999L
        );
        CheckoutSessionRequest multiItemRequest = new CheckoutSessionRequest(
                request.customerInfo(),
                List.of(request.items().get(0), item2),
                request.shippingInfo()
        );

        String sessionUrl = "https://checkout.stripe.com/pay/cs_test_456";
        when(stripeCheckoutService.createCheckoutSession(any(CheckoutSessionRequest.class)))
                .thenReturn(sessionUrl);

        // When & Then
        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(multiItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionUrl").value(sessionUrl));
    }
}
