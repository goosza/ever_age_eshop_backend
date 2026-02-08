package com.everage.eshop.dto;

import com.everage.eshop.entity.ShippingProvider;
import com.everage.eshop.entity.ShippingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ShippingResponse(
        UUID id,
        UUID orderId,
        ShippingProvider provider,
        BigDecimal cost,
        String trackingNumber,
        ShippingStatus status,
        LocalDateTime estimatedDelivery,
        LocalDateTime createdAt
) {}
