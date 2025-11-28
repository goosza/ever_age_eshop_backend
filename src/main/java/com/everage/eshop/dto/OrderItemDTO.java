package com.everage.eshop.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDTO(
        Long id,
        UUID itemId,
        String itemName,
        String itemDescription,
        String itemImageUrl,
        Integer quantity,
        BigDecimal price,
        BigDecimal subtotal
) {}