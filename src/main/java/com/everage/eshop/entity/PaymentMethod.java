package com.everage.eshop.entity;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CREDIT_CARD("Credit Card"),
    APPLE_PAY("Apple Pay"),
    BANK_TRANSFER("Bank Transfer");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

}
