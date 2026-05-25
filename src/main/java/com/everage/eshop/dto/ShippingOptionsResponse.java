package com.everage.eshop.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response containing available shipping methods and their costs
 */
public record ShippingOptionsResponse(
        List<ShippingMethodDto> methods,
        FreeShippingInfo freeShippingInfo
) {
    public record FreeShippingInfo(
            boolean enabled,
            BigDecimal threshold,
            BigDecimal remaining
    ) {}
}
