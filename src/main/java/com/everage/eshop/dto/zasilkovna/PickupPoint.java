package com.everage.eshop.dto.zasilkovna;

import lombok.Data;

import java.util.Map;

@Data
public class PickupPoint {
    private String id;
    private String name;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private Coordinates coordinates;
    private Map<String, String> openingHours;

    @Data
    public static class Coordinates {
        private Double latitude;
        private Double longitude;
    }
}
