package com.everage.eshop.dto;

import java.math.BigDecimal;
import java.util.UUID;
import com.everage.eshop.entity.ItemStatus;

public record ItemDto(
        UUID uuid,
        String name,
        String description,
        BigDecimal price,
        ItemStatus status,
        Integer quantity
) {}
