package com.everage.eshop.controller;

import com.everage.eshop.dto.OrderDto;
import com.everage.eshop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Tracking", description = "Order tracking and history API")
public class OrderTrackingController {

    private final OrderService orderService;

    @GetMapping("/track/{orderNumber}")
    @Operation(
            summary = "Track order by number",
            description = "Get order details and status by order number"
    )
    public OrderDto trackOrder(@PathVariable String orderNumber) {
        log.info("Tracking order: {}", orderNumber);
        return orderService.getOrderByNumber(orderNumber);
    }

    @GetMapping("/by-session/{sessionId}")
    @Operation(
            summary = "Get order by Stripe session ID",
            description = "Retrieve order details using Stripe checkout session ID. Used on success page to display order confirmation."
    )
    public OrderDto getOrderBySessionId(@PathVariable String sessionId) {
        log.info("Fetching order for Stripe session: {}", sessionId);
        return orderService.getOrderByStripeSessionId(sessionId);
    }

    @GetMapping("/customer/{email}")
    @Operation(
            summary = "Get customer orders",
            description = "Get all orders for a customer by email address"
    )
    public List<OrderDto> getCustomerOrders(@PathVariable String email) {
        log.info("Getting orders for customer: {}", email);
        return orderService.getOrdersByEmail(email);
    }
}
