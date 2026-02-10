package com.everage.eshop.dto;

import com.everage.eshop.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderStatusHistoryDto(
        Long id,
        OrderStatus oldStatus,
        OrderStatus newStatus,
        String oldStatusDisplay,
        String newStatusDisplay,
        String changedBy,
        String notes,
        LocalDateTime createdAt
) {}