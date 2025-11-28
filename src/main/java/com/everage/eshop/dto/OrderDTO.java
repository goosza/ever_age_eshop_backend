package com.everage.eshop.dto;

import com.everage.eshop.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
        Long id,
        String orderNumber,

        // Customer info
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate birthDate,

        // Address
        String address,
        String city,
        String postalCode,
        String country,

        // Order details
        List<OrderItemDTO> items,
        Integer itemsCount,
        BigDecimal totalAmount,
        OrderStatus status,
        String statusDisplay,

        // Shipping
        String shippingProvider,
        String trackingNumber,
        String trackingUrl,

        // Notes
        String customerNotes,
        String adminNotes,

        // Status history
        List<OrderStatusHistoryDTO> statusHistory,

        // Timestamps
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime confirmedAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt
) {}