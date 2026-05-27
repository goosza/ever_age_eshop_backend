package com.everage.eshop.dto;

import com.everage.eshop.entity.PaymentMethod;
import com.everage.eshop.entity.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPaymentDto(
        UUID uuid,
        PaymentMethod method,
        PaymentStatus status,
        BigDecimal amount
) {}
