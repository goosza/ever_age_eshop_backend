package com.everage.eshop.controller;

import com.everage.eshop.dto.OrderDto;
import com.everage.eshop.entity.OrderStatus;
import com.everage.eshop.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckoutController.class)
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void getOrderByNumber_ShouldReturn200WithOrder() throws Exception {
        OrderDto orderDto = createOrderDto();

        when(orderService.getOrderByNumber(anyString())).thenReturn(orderDto);

        mockMvc.perform(get("/api/checkout/order/{orderNumber}", "ORD-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-123"));
    }

    @Test
    void getOrdersByEmail_ShouldReturn200WithOrders() throws Exception {
        OrderDto orderDto = createOrderDto();

        when(orderService.getOrdersByEmail(anyString())).thenReturn(List.of(orderDto));

        mockMvc.perform(get("/api/checkout/email/{email}", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-123"));
    }

    private OrderDto createOrderDto() {
        return new OrderDto(
                UUID.randomUUID(),
                "ORD-123",
                "John",
                "Doe",
                "test@example.com",
                "+1234567890",
                "123 Main St",
                "New York",
                "10001",
                "USA",
                List.of(),
                BigDecimal.valueOf(110.00),
                OrderStatus.PENDING,
                "Please deliver after 5pm",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
