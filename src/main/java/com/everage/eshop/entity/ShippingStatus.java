package com.everage.eshop.entity;

import lombok.Getter;

@Getter
public enum ShippingStatus {
    PENDING("Pending"),
    CREATED("Created"),
    IN_TRANSIT("In Transit"),
    READY_FOR_PICKUP("Ready for Pickup"),
    DELIVERED("Delivered"),
    RETURNED("Returned");

    private final String displayName;

    ShippingStatus(String displayName) {
        this.displayName = displayName;
    }
}
