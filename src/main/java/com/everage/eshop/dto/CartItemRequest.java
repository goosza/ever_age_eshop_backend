package com.everage.eshop.dto;

import java.util.UUID;

public record CartItemRequest(UUID itemId,
                              Integer quantity) {
}
