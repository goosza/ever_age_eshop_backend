package com.everage.eshop.dto;

import java.util.List;
import java.util.UUID;

public record CartDto(UUID uuid,
                      List<CartItemDto> items) {
}