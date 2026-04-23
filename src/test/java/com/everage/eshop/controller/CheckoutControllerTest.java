package com.everage.eshop.controller;

import com.everage.eshop.dto.CheckoutRequest;
import com.everage.eshop.dto.CompleteCheckoutRequest;
import com.everage.eshop.dto.OrderDto;
import com.everage.eshop.entity.OrderStatus;
import com.everage.eshop.entity.PaymentMethod;
import com.everage.eshop.entity.ShippingProvider;
import com.everage.eshop.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckoutController.class)
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completeCheckout_ShouldReturn200WithOrder() throws Exception {
        CompleteCheckoutRequest request = new CompleteCheckoutRequest(
                createCheckoutRequest(),
                PaymentMethod.CREDIT_CARD,
                "tok_visa",
                ShippingProvider.ZASILKOVNA
        );
        OrderDto orderDto = createOrderDto();

        when(orderService.completeCheckout(any(), eq(PaymentMethod.CREDIT_CARD),
                eq("tok_visa"), eq(ShippingProvider.ZASILKOVNA)))
                .thenReturn(orderDto);

        mockMvc.perform(post("/api/checkout/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderNumber").value("EVE-2024-000001"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void completeCheckout_WhenPaymentFails_ShouldReturn500() throws Exception {
        CompleteCheckoutRequest request = new CompleteCheckoutRequest(
                createCheckoutRequest(),
                PaymentMethod.CREDIT_CARD,
                "invalid_token",
                ShippingProvider.ZASILKOVNA
        );

        when(orderService.completeCheckout(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Payment processing failed"));

        mockMvc.perform(post("/api/checkout/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getOrderByNumber_WhenExists_ShouldReturn200() throws Exception {
        String orderNumber = "EVE-2024-000001";
        when(orderService.getOrderByNumber(orderNumber)).thenReturn(createOrderDto());

        mockMvc.perform(get("/api/checkout/order/{orderNumber}", orderNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value(orderNumber));
    }

    @Test
    void getOrderByNumber_WhenNotFound_ShouldReturn500() throws Exception {
        String orderNumber = "EVE-2024-999999";
        when(orderService.getOrderByNumber(orderNumber))
                .thenThrow(new RuntimeException("Order not found: " + orderNumber));

        mockMvc.perform(get("/api/checkout/order/{orderNumber}", orderNumber))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getOrdersByEmail_ShouldReturnList() throws Exception {
        String email = "john@example.com";
        when(orderService.getOrdersByEmail(email)).thenReturn(List.of(createOrderDto()));

        mockMvc.perform(get("/api/checkout/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email").value("john@example.com"));
    }

    @Test
    void getOrdersByEmail_WhenNoOrders_ShouldReturnEmptyList() throws Exception {
        String email = "nobody@example.com";
        when(orderService.getOrdersByEmail(email)).thenReturn(List.of());

        mockMvc.perform(get("/api/checkout/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CheckoutRequest createCheckoutRequest() {
        return new CheckoutRequest(
                "John", "Doe", "john@example.com", "+420123456789",
                "Main St 1", "Prague", "11000", "CZ",
                List.of(), null
        );
    }

    private OrderDto createOrderDto() {
        return new OrderDto(
                UUID.randomUUID(), "EVE-2024-000001",
                "John", "Doe", "john@example.com", "+420123456789",
                "Main St 1", "Prague", "11000", "CZ",
                List.of(), BigDecimal.valueOf(99.99), OrderStatus.CONFIRMED,
                null, LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
