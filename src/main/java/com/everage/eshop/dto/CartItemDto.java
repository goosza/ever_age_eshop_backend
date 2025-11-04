package com.everage.eshop.dto;

import java.util.UUID;

public record CartItemDto(UUID uuid,
                          ItemDto item,
                          Integer quantity) {
}