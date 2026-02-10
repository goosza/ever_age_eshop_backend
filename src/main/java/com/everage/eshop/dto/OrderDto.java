package com.everage.eshop.dto;

import com.everage.eshop.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDto(
        UUID id,
        String orderNumber,

        // Customer info
        String firstName,
        String lastName,
        String email,
        String phone,

        // Address
        String address,
        String city,
        String postalCode,
        String country,

        // Order details
        List<OrderItemDto> items,
        BigDecimal totalAmount,
        OrderStatus status,

        // Notes
        String customerNotes,

        // Timestamps
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}