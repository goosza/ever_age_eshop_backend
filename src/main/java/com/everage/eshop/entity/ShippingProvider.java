package com.everage.eshop.entity;

import lombok.Getter;

@Getter
public enum ShippingProvider {
    ZASILKOVNA("Zasilkovna"),
    DPD("DPD"),
    FEDEX("FedEx"),
    UPS("UPS"),
    STANDARD("Standard Delivery");

    private final String displayName;

    ShippingProvider(String displayName) {
        this.displayName = displayName;
    }

}
