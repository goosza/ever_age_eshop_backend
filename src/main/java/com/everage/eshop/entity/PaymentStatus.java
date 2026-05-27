package com.everage.eshop.entity;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    COMPLETED("Completed"),
    REFUNDED("Refunded"),
    CANCELLED("Cancelled");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
}
