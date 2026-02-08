package com.everage.eshop.dto;

import com.everage.eshop.entity.PaymentMethod;
import com.everage.eshop.entity.ShippingProvider;

public record CompleteCheckoutRequest(
        CheckoutRequest checkoutRequest,
        PaymentMethod paymentMethod,
        String paymentToken,
        ShippingProvider shippingProvider
) {}
