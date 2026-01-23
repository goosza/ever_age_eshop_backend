package com.everage.eshop.dto;

import java.util.UUID;

public record OrderItemRequest(
        UUID uuid,
        Integer quantity
) {
}
