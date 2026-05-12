package com.everage.eshop.dto;

import com.everage.eshop.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderStatusHistoryDto(
        UUID uuid,
        OrderStatus oldStatus,
        OrderStatus newStatus,
        String oldStatusDisplay,
        String newStatusDisplay,
        String changedBy,
        String notes,
        LocalDateTime createdAt
) {}