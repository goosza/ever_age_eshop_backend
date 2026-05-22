package com.everage.eshop.dto;

import java.util.List;

/**
 * Internal DTO for creating orders.
 * Used by services (e.g., StripeWebhookService) to create orders.
 */
public record CreateOrderRequest(
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        String city,
        String postalCode,
        String country,
        List<OrderItemRequest> items,
        String customerNotes
) {
}
