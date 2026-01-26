package com.everage.eshop.dto;

import com.everage.eshop.entity.ShippingProvider;
import java.util.UUID;

public record ShippingRequest(
        UUID orderId,
        ShippingProvider provider,
        String address
) {}
