package com.everage.eshop.dto;

import com.everage.eshop.entity.ShippingProvider;
import java.math.BigDecimal;
import java.util.UUID;

public record ShippingRequest(
        UUID orderId,
        ShippingProvider provider,
        String address,
        BigDecimal cost,
        String pickupPointId,
        String pickupPointName,
        String pickupPointAddress
) {}
