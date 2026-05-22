package com.everage.eshop.entity;

import lombok.Getter;

@Getter
public enum ShippingProvider {
    ZASILKOVNA("Zásilkovna"),
    DHL("DHL Express"),
    FEDEX("FedEx"),
    UPS("UPS"),
    CESKA_POSTA("Česká pošta"),
    PPL("PPL"),
    DPD("DPD"),
    GLS("GLS"),
    STANDARD("Standard Shipping");

    private final String displayName;

    private ShippingProvider(String name){
        this.displayName = name;
    }
}
