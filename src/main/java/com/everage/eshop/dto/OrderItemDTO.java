package com.everage.eshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderItemDTO(
        UUID id,
        UUID itemUuid,
        Integer quantity,
        BigDecimal price,
        LocalDateTime createdAt
) {}