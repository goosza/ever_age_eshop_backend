package com.everage.eshop.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin Orders", description = "Order management for administrators")
public class AdminOrderController {

//    private final AdminOrderService adminOrderService;
//
//    @GetMapping
//    @Operation(summary = "Get all orders")
//    public ResponseEntity<Page<OrderDTO>> getAllOrders(
//            @RequestParam(required = false) OrderStatus status,
//            @RequestParam(required = false) String searchQuery,
//            Pageable pageable
//    ) {
//        Page<OrderDTO> orders = adminOrderService.getAllOrders(status, searchQuery, pageable);
//        return ResponseEntity.ok(orders);
//    }
//
//    @GetMapping("/{orderId}")
//    @Operation(summary = "Get order by ID")
//    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long orderId) {
//        OrderDTO order = adminOrderService.getOrder(orderId);
//        return ResponseEntity.ok(order);
//    }
//
//    @PostMapping("/{orderId}/confirm")
//    @Operation(summary = "Confirm order")
//    public OrderDTO confirmOrder(
//            @PathVariable UUID orderId,
//            @RequestBody(required = false) AdminNoteRequest request
//    ) {
//        return adminOrderService.confirmOrder(orderId, request);
//    }
//
//    @PostMapping("/{orderId}/ship")
//    @Operation(summary = "Ship order")
//    public OrderDTO shipOrder(
//            @PathVariable Long orderId,
//            @RequestBody ShipOrderRequest request
//    ) {
//        return adminOrderService.shipOrder(orderId, request);
//    }
//
//    @PostMapping("/{orderId}/deliver")
//    @Operation(summary = "Mark as delivered")
//    public OrderDTO deliverOrder(@PathVariable UUID orderId) {
//        return adminOrderService.deliverOrder(orderId);
//    }
//
//    @PostMapping("/{orderId}/cancel")
//    @Operation(summary = "Cancel order")
//    public OrderDTO cancelOrder(
//            @PathVariable Long orderId,
//            @RequestBody AdminNoteRequest request
//    ) {
//        return adminOrderService.cancelOrder(orderId, request);
//    }
//
//    @PatchMapping("/{orderId}/status")
//    @Operation(summary = "Update order status")
//    public OrderDTO updateStatus(
//            @PathVariable Long orderId,
//            @RequestBody UpdateStatusRequest request
//    ) {
//        return adminOrderService.updateStatus(orderId, request);
//    }
//
//    @PostMapping("/{orderId}/notes")
//    @Operation(summary = "Add admin note")
//    public OrderDTO addNote(
//            @PathVariable Long orderId,
//            @RequestBody AdminNoteRequest request
//    ) {
//        return adminOrderService.addAdminNote(orderId, request);
//    }

//    @GetMapping("/stats")
//    @Operation(summary = "Get order statistics")
//    public ResponseEntity<OrderStatsDTO> getStats() {
//        OrderStatsDTO stats = adminOrderService.getOrderStats();
//        return ResponseEntity.ok(stats);
//    }
}