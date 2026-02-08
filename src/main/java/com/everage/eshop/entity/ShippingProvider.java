package com.everage.eshop.entity;

import lombok.Getter;

@Getter
public enum ShippingProvider {
    ZASILKOVNA("Zasilkovna");

    private final String displayName;

    private ShippingProvider(String name){
        this.displayName = name;
    }
}
