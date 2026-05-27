package com.everage.eshop.dto;

import com.everage.eshop.entity.ShippingProvider;
import com.everage.eshop.entity.ShippingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ShippingResponse(
        UUID uuid,
        UUID orderUuid,
        ShippingProvider provider,
        BigDecimal cost,
        String trackingNumber,
        ShippingStatus status,
        LocalDateTime estimatedDelivery,
        // Delivery address (for HOME delivery)
        String address,
        String city,
        String postalCode,
        String country,
        // Pickup point (for PICKUP/ZBOX/CARRIER_PICKUP)
        String pickupPointId,
        String pickupPointName,
        String pickupPointAddress,
        String shipmentId,
        String labelUrl,
        LocalDateTime createdAt
) {}
