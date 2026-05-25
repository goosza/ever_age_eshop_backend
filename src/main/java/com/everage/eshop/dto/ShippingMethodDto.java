package com.everage.eshop.dto;

import java.math.BigDecimal;

/**
 * DTO for shipping method with pricing information
 */
public record ShippingMethodDto(
        String method,           // "PICKUP", "ZBOX", "HOME", "CARRIER_PICKUP"
        String name,             // Display name
        String description,      // Description for UI
        BigDecimal cost,         // Cost in EUR
        String provider,         // "ZASILKOVNA", etc.
        boolean available        // Is this method currently available
) {
}
