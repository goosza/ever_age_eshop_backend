package com.everage.eshop.dto;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutSessionRequest(
        CustomerInfo customerInfo,
        List<CheckoutItem> items,
        ShippingInfo shippingInfo
) {
    public record CustomerInfo(
            String firstName,
            String lastName,
            String email,
            String phone,
            String country,
            // Optional - only for HOME delivery
            String address,
            String city,
            String postalCode
    ) {}

    public record CheckoutItem(
            String itemUuid,
            Integer quantity
    ) {}
    
    public record ShippingInfo(
            String provider,      // "ZASILKOVNA", "OTHER"
            String method,        // "PICKUP", "ZBOX", "HOME", "CARRIER_PICKUP", "STANDARD"
            BigDecimal cost,
            // For pickup methods (PICKUP, ZBOX, CARRIER_PICKUP)
            String pickupPointId,
            String pickupPointName,
            String pickupPointAddress
    ) {}
}
