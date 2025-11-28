package com.everage.eshop.controller;

import com.everage.eshop.dto.CheckoutRequest;
import com.everage.eshop.dto.OrderDTO;
import com.everage.eshop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Order checkout and tracking API")
public class CheckoutController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create order", description = "Creates a new order from checkout data")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CheckoutRequest request) {
        OrderDTO order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders/{orderNumber}")
    @Operation(summary = "Get order by number")
    public ResponseEntity<OrderDTO> getOrderByNumber(@PathVariable String orderNumber) {
        OrderDTO order = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders/by-email")
    @Operation(summary = "Get orders by email")
    public ResponseEntity<List<OrderDTO>> getOrdersByEmail(@RequestParam String email) {
        List<OrderDTO> orders = orderService.getOrdersByEmail(email);
        return ResponseEntity.ok(orders);
    }
}