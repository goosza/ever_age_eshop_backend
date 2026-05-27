package com.everage.eshop.dto;

import com.everage.eshop.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDto(
        UUID uuid,
        String orderNumber,

        // Customer info
        String firstName,
        String lastName,
        String email,
        String phone,

        // Order details
        List<OrderItemDto> items,
        BigDecimal totalAmount,
        OrderStatus status,

        // Notes
        String customerNotes,

        // Shipping info (null until shipping is created)
        OrderShippingDto shipping,

        // Payment info (null until payment is processed)
        OrderPaymentDto payment,

        // Timestamps
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
