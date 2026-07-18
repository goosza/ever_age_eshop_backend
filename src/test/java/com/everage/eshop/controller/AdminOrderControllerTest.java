package com.everage.eshop.controller;

import com.everage.eshop.config.TestSecurityConfig;
import com.everage.eshop.dto.OrderDto;
import com.everage.eshop.entity.OrderStatus;
import com.everage.eshop.service.AdminOrderService;
import com.everage.eshop.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminOrderController.class)
@Import(TestSecurityConfig.class)
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminOrderService adminOrderService;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllOrders_UsesDefaultPageAndSize() throws Exception {
        var page = new PageImpl<>(List.of(createOrderDto()), PageRequest.of(0, 20), 1);
        when(adminOrderService.getAllOrders(isNull(), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderNumber").value("EVE-2026-ABCDEFGHJK"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(adminOrderService).getAllOrders(isNull(), eq(PageRequest.of(0, 20, Sort.by("createdAt").descending())));
    }

    @Test
    void getAllOrders_WithStatusFilter_PassesStatusThrough() throws Exception {
        var page = new PageImpl<OrderDto>(List.of(), PageRequest.of(0, 20), 0);
        when(adminOrderService.getAllOrders(eq(OrderStatus.SHIPPED), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/orders?status=SHIPPED"))
                .andExpect(status().isOk());

        verify(adminOrderService).getAllOrders(eq(OrderStatus.SHIPPED), any(PageRequest.class));
    }

    @Test
    void getAllOrders_WithCustomPageAndSize_UsesThem() throws Exception {
        var page = new PageImpl<OrderDto>(List.of(), PageRequest.of(3, 10), 0);
        when(adminOrderService.getAllOrders(isNull(), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/orders?page=3&size=10"))
                .andExpect(status().isOk());

        verify(adminOrderService).getAllOrders(isNull(), eq(PageRequest.of(3, 10, Sort.by("createdAt").descending())));
    }

    @Test
    void getAllOrders_ClampsPageSizeAboveMaximum() throws Exception {
        var page = new PageImpl<OrderDto>(List.of(), PageRequest.of(0, 100), 0);
        when(adminOrderService.getAllOrders(isNull(), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/orders?size=99999"))
                .andExpect(status().isOk());

        // Requested size far exceeds the max — must be clamped to 100, not passed through raw.
        verify(adminOrderService).getAllOrders(isNull(), eq(PageRequest.of(0, 100, Sort.by("createdAt").descending())));
    }

    @Test
    void getAllOrders_ClampsNegativePageToZero() throws Exception {
        var page = new PageImpl<OrderDto>(List.of(), PageRequest.of(0, 20), 0);
        when(adminOrderService.getAllOrders(isNull(), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/orders?page=-5"))
                .andExpect(status().isOk());

        verify(adminOrderService).getAllOrders(isNull(), eq(PageRequest.of(0, 20, Sort.by("createdAt").descending())));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private OrderDto createOrderDto() {
        return new OrderDto(
                UUID.randomUUID(), "EVE-2026-ABCDEFGHJK", "John", "Doe", "john@example.com", null,
                List.of(), BigDecimal.TEN, OrderStatus.PENDING, null, null, null, null, null
        );
    }
}
