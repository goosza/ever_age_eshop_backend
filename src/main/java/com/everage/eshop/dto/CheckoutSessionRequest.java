package com.everage.eshop.dto;

import java.util.List;

public record CheckoutSessionRequest(
        CustomerInfo customerInfo,
        List<CheckoutItem> items
) {
    public record CustomerInfo(
            String firstName,
            String lastName,
            String email,
            String phone,
            String address,
            String city,
            String postalCode,
            String country
    ) {}

    public record CheckoutItem(
            String productId,
            Integer quantity,
            Long price  // в центах
    ) {}
}
