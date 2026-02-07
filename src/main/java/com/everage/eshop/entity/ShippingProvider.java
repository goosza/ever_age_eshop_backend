package com.everage.eshop.entity;

import lombok.Getter;

@Getter
public enum ShippingProvider {
    ShippingProvider("Zasilkovna");

    private final String displayName;

    private ShippingProvider(String name){
        this.displayName = name;
    }
}
