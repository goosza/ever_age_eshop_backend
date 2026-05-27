package com.everage.eshop.dto;

import java.util.List;

/**
 * Internal DTO for creating orders.
 * Used by services (e.g., StripeWebhookService) to create orders.
 * Address fields are NOT here - they belong to Shipping entity.
 */
public record CreateOrderRequest(
        String firstName,
        String lastName,
        String email,
        String phone,
        List<OrderItemRequest> items,
        String customerNotes
) {}
