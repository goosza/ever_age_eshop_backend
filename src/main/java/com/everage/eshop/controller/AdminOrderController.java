package com.everage.eshop.controller;

import com.everage.eshop.dto.OrderDto;
import com.everage.eshop.entity.OrderStatus;
import com.everage.eshop.service.AdminOrderService;
import com.everage.eshop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    private final OrderService orderService;

    private static final int MAX_PAGE_SIZE = 100;

    @GetMapping
    @Operation(
            summary = "Get orders (paginated)",
            description = "Returns a page of orders, optionally filtered by status. "
                    + "Defaults to page 0, size 20, sorted by createdAt descending."
    )
    public Page<OrderDto> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max " + MAX_PAGE_SIZE + ")") @RequestParam(defaultValue = "20") int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        log.info("Admin: fetching orders page {} size {}, status filter: {}", safePage, safeSize, status);
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());
        return adminOrderService.getAllOrders(status, pageRequest);
    }

    @GetMapping("/customer/{email}")
    @Operation(summary = "Get orders by customer email")
    public List<OrderDto> getCustomerOrders(@PathVariable String email) {
        log.info("Admin: fetching orders for customer: {}", email);
        return orderService.getOrdersByEmail(email);
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
