package com.everage.eshop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for shipping pricing with zone-based pricing
 */
@Configuration
@ConfigurationProperties(prefix = "shipping.pricing")
@Data
public class ShippingPricingConfig {
    // Default prices for Zone 1 (Czech Republic)
    private BigDecimal pickup = new BigDecimal("69.00");
    private BigDecimal zbox = new BigDecimal("59.00");
    private BigDecimal home = new BigDecimal("119.00");
    private BigDecimal carrierPickup = new BigDecimal("79.00");
    
    // Free shipping configuration
    private FreeShipping freeShipping = new FreeShipping();
    
    // Zone-based pricing
    private Map<String, Zone> zones = new HashMap<>();
    
    @Data
    public static class FreeShipping {
        private boolean enabled = false;
        private BigDecimal minOrderAmount = new BigDecimal("1000.00");
    }
    
    @Data
    public static class Zone {
        private List<String> countries;
        private BigDecimal pickup;
        private BigDecimal zbox;
        private BigDecimal home;
        private BigDecimal carrierPickup;
    }
    
    /**
     * Get zone for country
     */
    public String getZoneForCountry(String country) {
        if (country == null) return "zone1";
        
        String countryUpper = country.toUpperCase();
        
        for (Map.Entry<String, Zone> entry : zones.entrySet()) {
            Zone zone = entry.getValue();
            if (zone.getCountries() != null && zone.getCountries().contains(countryUpper)) {
                return entry.getKey();
            }
        }
        
        return "zone1"; // Default to zone1
    }
    
    /**
     * Get pricing for specific country using zones
     */
    public BigDecimal getPickupForCountry(String country) {
        String zone = getZoneForCountry(country);
        Zone zoneConfig = zones.get(zone);
        return zoneConfig != null && zoneConfig.getPickup() != null
                ? zoneConfig.getPickup()
                : pickup;
    }
    
    public BigDecimal getZboxForCountry(String country) {
        String zone = getZoneForCountry(country);
        Zone zoneConfig = zones.get(zone);
        return zoneConfig != null && zoneConfig.getZbox() != null
                ? zoneConfig.getZbox()
                : zbox;
    }
    
    public BigDecimal getHomeForCountry(String country) {
        String zone = getZoneForCountry(country);
        Zone zoneConfig = zones.get(zone);
        return zoneConfig != null && zoneConfig.getHome() != null
                ? zoneConfig.getHome()
                : home;
    }
    
    public BigDecimal getCarrierPickupForCountry(String country) {
        String zone = getZoneForCountry(country);
        Zone zoneConfig = zones.get(zone);
        return zoneConfig != null && zoneConfig.getCarrierPickup() != null
                ? zoneConfig.getCarrierPickup()
                : carrierPickup;
    }
}
