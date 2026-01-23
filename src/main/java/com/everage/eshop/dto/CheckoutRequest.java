package com.everage.eshop.dto;

import java.util.List;

public record CheckoutRequest(
        // Customer Information
        String firstName,
        String lastName,
        String email,
        String phone,

        // Delivery Address
        String address,
        String city,
        String postalCode,
        String country,

        // Cart Items
        List<OrderItemRequest> items,

        // Optional Notes
        String customerNotes
) {
}