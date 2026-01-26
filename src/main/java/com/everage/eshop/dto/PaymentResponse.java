package com.everage.eshop.dto;

import com.everage.eshop.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        BigDecimal amount,
        PaymentStatus status,
        String paymentReference,
        LocalDateTime createdAt
) {}
