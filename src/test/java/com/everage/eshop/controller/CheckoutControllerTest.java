package com.everage.eshop.controller;

import com.everage.eshop.dto.CheckoutSessionRequest;
import com.everage.eshop.dto.CheckoutSessionResponse;
import com.everage.eshop.service.stripe.StripeCheckoutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckoutController.class)
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StripeCheckoutService stripeCheckoutService;

    @Test
    void createCheckoutSession_ShouldReturn200WithSessionUrl() throws Exception {
        // Given
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
                "123e4567-e89b-12d3-a456-426614174000",
                2
        );

        CheckoutSessionRequest.ShippingInfo shippingInfo = new CheckoutSessionRequest.ShippingInfo(
                "ZASILKOVNA",
                "PICKUP",
                BigDecimal.valueOf(12.00),
                "12345",
                "Zasilkovna Prague",
                "Central Square 1"
        );

        CheckoutSessionRequest request = new CheckoutSessionRequest(customerInfo, List.of(checkoutItem), shippingInfo);
        String mockSessionUrl = "https://checkout.stripe.com/pay/cs_test_123";

        when(stripeCheckoutService.createCheckoutSession(any(CheckoutSessionRequest.class)))
                .thenReturn(mockSessionUrl);

        // When & Then
        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionUrl").value(mockSessionUrl));
    }
}
