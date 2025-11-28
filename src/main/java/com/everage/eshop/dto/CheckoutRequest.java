package com.everage.eshop.dto;

import java.time.LocalDate;
import java.util.List;

public record CheckoutRequest(
        // Customer Information
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate birthDate,

        // Delivery Address
        String address,
        String city,
        String postalCode,
        String country,

        // Cart Items
        List<CartItemRequest> items,

        // Optional Notes
        String customerNotes
) {
}