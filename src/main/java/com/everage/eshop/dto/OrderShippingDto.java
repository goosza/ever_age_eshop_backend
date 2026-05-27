package com.everage.eshop.dto;

import com.everage.eshop.entity.ShippingProvider;
import com.everage.eshop.entity.ShippingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderShippingDto(
        UUID uuid,
        ShippingProvider provider,
        ShippingStatus status,
        String trackingNumber,
        LocalDateTime estimatedDelivery,
        // Delivery address (HOME delivery)
        String address,
        String city,
        String postalCode,
        String country,
        // Pickup point (PICKUP/ZBOX/CARRIER_PICKUP)
        String pickupPointId,
        String pickupPointName,
        String pickupPointAddress
) {}
