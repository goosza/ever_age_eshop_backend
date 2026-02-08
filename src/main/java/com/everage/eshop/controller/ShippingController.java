package com.everage.eshop.controller;

import com.everage.eshop.dto.ShippingRequest;
import com.everage.eshop.dto.ShippingResponse;
import com.everage.eshop.entity.ShippingStatus;
import com.everage.eshop.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@Tag(name = "Shipping", description = "Shipping management API")
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping("/create")
    @Operation(summary = "Create shipping", description = "Create shipping for an order")
    public ShippingResponse createShipping(@RequestBody ShippingRequest request) {
        return shippingService.createShipping(request);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get shipping by order", description = "Get shipping information for an order")
    public ShippingResponse getShippingByOrder(@PathVariable UUID orderId) {
        return shippingService.getShippingByOrderId(orderId);
    }

    @PatchMapping("/{shippingId}/status")
    @Operation(summary = "Update shipping status", description = "Update shipping status")
    public ShippingResponse updateShippingStatus(
            @PathVariable UUID shippingId,
            @RequestParam ShippingStatus status) {
        return shippingService.updateShippingStatus(shippingId, status);
    }
}
