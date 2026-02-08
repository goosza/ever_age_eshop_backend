package com.everage.eshop.entity;

import lombok.Getter;

@Getter
public enum ShippingStatus {
    PENDING("Pending"),
    PICKED("Picked"),
    SHIPPED("Shipped"),
    IN_TRANSIT("In Transit"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    FAILED("Failed"),
    RETURNED("Returned");

    private final String displayName;

    ShippingStatus(String displayName) {
        this.displayName = displayName;
    }
}
