package com.everage.eshop.controller;

import com.everage.eshop.dto.OrderDto;
import com.everage.eshop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Order tracking API")
public class CheckoutController {

    private final OrderService orderService;

    @GetMapping("/order/{orderNumber}")
    @Operation(summary = "Get order by number")
    public OrderDto getOrderByNumber(@PathVariable String orderNumber) {
        return orderService.getOrderByNumber(orderNumber);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get orders by email")
    public List<OrderDto> getOrdersByEmail(@PathVariable String email) {
        return orderService.getOrdersByEmail(email);
    }
}
