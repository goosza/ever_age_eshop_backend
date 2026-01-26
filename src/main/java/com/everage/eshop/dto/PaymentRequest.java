package com.everage.eshop.dto;

import com.everage.eshop.entity.PaymentMethod;
import java.util.UUID;

public record PaymentRequest(
        UUID orderId,
        PaymentMethod method,
        String paymentToken
) {}
