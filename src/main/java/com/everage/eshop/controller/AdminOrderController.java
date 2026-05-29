package com.everage.eshop.controller;

import com.everage.eshop.dto.OrderDto;
import com.everage.eshop.entity.OrderStatus;
import com.everage.eshop.service.AdminOrderService;
import com.everage.eshop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Orders", description = "Order management for administrators")
@SecurityRequirement(name = "hmac-auth")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get all orders", description = "Returns all orders, optionally filtered by status")
    public List<OrderDto> getAllOrders(
            @RequestParam(required = false) OrderStatus status
    ) {
        log.info("Admin: fetching all orders, status filter: {}", status);
        return adminOrderService.getAllOrders(status);
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Get order by UUID")
    public OrderDto getOrder(@PathVariable UUID uuid) {
        log.info("Admin: fetching order: {}", uuid);
        return adminOrderService.getOrder(uuid);
    }

    @PostMapping("/{uuid}/ship")
    @Operation(summary = "Mark order as shipped")
    public OrderDto shipOrder(@PathVariable UUID uuid) {
        log.info("Admin: shipping order: {}", uuid);
        return adminOrderService.shipOrder(uuid);
    }

    @PostMapping("/{uuid}/deliver")
    @Operation(summary = "Mark order as delivered")
    public OrderDto deliverOrder(@PathVariable UUID uuid) {
        log.info("Admin: delivering order: {}", uuid);
        return adminOrderService.deliverOrder(uuid);
    }

    @PostMapping("/{uuid}/cancel")
    @Operation(summary = "Cancel order")
    public OrderDto cancelOrder(@PathVariable UUID uuid) {
        log.info("Admin: cancelling order: {}", uuid);
        return adminOrderService.cancelOrder(uuid);
    }
}
