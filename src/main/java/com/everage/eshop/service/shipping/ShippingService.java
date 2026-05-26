package com.everage.eshop.service.shipping;

import com.everage.eshop.config.ShippingPricingConfig;
import com.everage.eshop.config.ZasilkovnaConfig;
import com.everage.eshop.dto.CountryDto;
import com.everage.eshop.dto.ShippingMethodDto;
import com.everage.eshop.dto.ShippingOptionsResponse;
import com.everage.eshop.dto.ShippingRequest;
import com.everage.eshop.dto.ShippingResponse;
import com.everage.eshop.dto.mapper.ShippingMapper;
import com.everage.eshop.dto.zasilkovna.CreateShipmentRequest;
import com.everage.eshop.dto.zasilkovna.CreateShipmentResponse;
import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.Shipping;
import com.everage.eshop.entity.ShippingProvider;
import com.everage.eshop.entity.ShippingStatus;
import com.everage.eshop.exception.order.OrderNotFoundException;
import com.everage.eshop.repository.OrderRepository;
import com.everage.eshop.repository.ShippingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShippingRepository shippingRepository;
    private final OrderRepository orderRepository;
    private final ShippingMapper shippingMapper;
    private final ShippingGatewayFactory gatewayFactory;
    private final ShippingPricingConfig pricingConfig;

    /**
     * Get available shipping methods and their costs
     */
    @Transactional(readOnly = true)
    public ShippingOptionsResponse getShippingOptions(String country, BigDecimal orderTotal) {
        log.info("Fetching shipping options for country: {}, orderTotal: {}", country, orderTotal);

        List<ShippingMethodDto> methods = new ArrayList<>();

        // Zasilkovna methods (available for CZ, SK, HU, RO, PL, AT, and other EU countries)
        boolean zasilkovnaAvailable = country == null || isZasilkovnaAvailable(country);

        if (zasilkovnaAvailable) {
            // Use country-specific pricing if available
            String countryCode = country != null ? country.toUpperCase() : "CZ";
            
            // Check if free shipping applies
            boolean isFreeShipping = orderTotal != null 
                && pricingConfig.getFreeShipping().isEnabled()
                && orderTotal.compareTo(pricingConfig.getFreeShipping().getMinOrderAmount()) >= 0;
            
            BigDecimal pickupCost = isFreeShipping ? BigDecimal.ZERO : pricingConfig.getPickupForCountry(countryCode);
            BigDecimal zboxCost = isFreeShipping ? BigDecimal.ZERO : pricingConfig.getZboxForCountry(countryCode);
            BigDecimal homeCost = isFreeShipping ? BigDecimal.ZERO : pricingConfig.getHomeForCountry(countryCode);
            BigDecimal carrierCost = isFreeShipping ? BigDecimal.ZERO : pricingConfig.getCarrierPickupForCountry(countryCode);
            
            // Check which methods are available for this country
            boolean hasOwnPickupPoints = hasZasilkovnaPickupPoints(countryCode);
            boolean hasZBox = hasZasilkovnaZBox(countryCode);
            boolean hasCarrierNetwork = hasCarrierPickupPoints(countryCode);
            
            // Zásilkovna own pick-up points (CZ, SK, HU, RO, PL, AT)
            if (hasOwnPickupPoints) {
                methods.add(new ShippingMethodDto(
                        "PICKUP",
                        "Pick-up Point",
                        "Zásilkovna pick-up point, COD available",
                        pickupCost,
                        "ZASILKOVNA",
                        true
                ));
            }

            // Z-BOX automated lockers (only CZ, SK)
            if (hasZBox) {
                methods.add(new ShippingMethodDto(
                        "ZBOX",
                        "Z-BOX (24/7)",
                        "Automated locker, 24/7 access",
                        zboxCost,
                        "ZASILKOVNA",
                        true
                ));
            }

            // Carrier pick-up points (partner networks in other countries)
            if (hasCarrierNetwork) {
                methods.add(new ShippingMethodDto(
                        "CARRIER_PICKUP",
                        "Carrier Pick-up",
                        "Partner carrier locations",
                        carrierCost,
                        "ZASILKOVNA",
                        true
                ));
            }

            // Home delivery (available everywhere)
            methods.add(new ShippingMethodDto(
                    "HOME",
                    "Home Delivery",
                    "Direct to your address",
                    homeCost,
                    "ZASILKOVNA",
                    true
            ));
        }

        // Calculate free shipping info
        ShippingOptionsResponse.FreeShippingInfo freeShippingInfo = null;
        if (pricingConfig.getFreeShipping().isEnabled()) {
            BigDecimal threshold = pricingConfig.getFreeShipping().getMinOrderAmount();
            BigDecimal remaining = orderTotal != null 
                ? threshold.subtract(orderTotal).max(BigDecimal.ZERO)
                : threshold;
            
            freeShippingInfo = new ShippingOptionsResponse.FreeShippingInfo(
                true,
                threshold,
                remaining
            );
        }

        log.info("Returning {} shipping methods for country: {}", methods.size(), country);
        return new ShippingOptionsResponse(methods, freeShippingInfo);
    }
    
    /**
     * Check if Zasilkovna is available for the country
     */
    private boolean isZasilkovnaAvailable(String country) {
        if (country == null) return true;
        
        // Zasilkovna operates in these EU countries
        String[] supportedCountries = {
            "CZ", "SK", "HU", "RO", "PL", "AT", "DE", "SI", "HR", "BG",
            "EE", "LV", "LT", "IT", "ES", "PT", "FR", "BE", "NL", "LU",
            "DK", "SE", "FI", "IE", "GR", "CY", "MT"
        };
        
        String countryUpper = country.toUpperCase();
        for (String supported : supportedCountries) {
            if (supported.equals(countryUpper)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if Zásilkovna has own pick-up points in the country
     * Own network: CZ, SK, HU, RO, PL, AT
     */
    private boolean hasZasilkovnaPickupPoints(String country) {
        if (country == null) return true;
        
        String[] ownNetworkCountries = {"CZ", "SK", "HU", "RO", "PL", "AT"};
        String countryUpper = country.toUpperCase();
        
        for (String supported : ownNetworkCountries) {
            if (supported.equals(countryUpper)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if Z-BOX automated lockers are available
     * Z-BOX only in: CZ, SK
     */
    private boolean hasZasilkovnaZBox(String country) {
        if (country == null) return true;
        
        String[] zboxCountries = {"CZ", "SK"};
        String countryUpper = country.toUpperCase();
        
        for (String supported : zboxCountries) {
            if (supported.equals(countryUpper)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if carrier pick-up points (partner networks) are available
     * Available in countries without own Zásilkovna network
     */
    private boolean hasCarrierPickupPoints(String country) {
        if (country == null) return false;
        
        // Carrier network available in countries where Zásilkovna operates
        // but doesn't have own pick-up points
        String countryUpper = country.toUpperCase();
        
        // Has Zásilkovna service but no own pick-up points
        return isZasilkovnaAvailable(countryUpper) && !hasZasilkovnaPickupPoints(countryUpper);
    }

    /**
     * Calculate shipping cost based on order total and method
     * Supports free shipping for orders above threshold
     */
    public BigDecimal calculateShippingCost(BigDecimal orderTotal, String method) {
        log.info("Calculating shipping cost for order total: {} EUR, method: {}", orderTotal, method);

        // Check if free shipping is enabled and order qualifies
        if (pricingConfig.getFreeShipping().isEnabled()) {
            BigDecimal minAmount = pricingConfig.getFreeShipping().getMinOrderAmount();
            if (orderTotal.compareTo(minAmount) >= 0) {
                log.info("Free shipping applied - order total {} >= {}", orderTotal, minAmount);
                return BigDecimal.ZERO;
            }
        }

        // Return base cost for the method
        BigDecimal cost = switch (method) {
            case "PICKUP" -> pricingConfig.getPickup();
            case "ZBOX" -> pricingConfig.getZbox();
            case "HOME" -> pricingConfig.getHome();
            case "CARRIER_PICKUP" -> pricingConfig.getCarrierPickup();
            default -> pricingConfig.getPickup();
        };

        log.info("Shipping cost calculated: {} EUR", cost);
        return cost;
    }

    @Transactional
    public ShippingResponse createShipping(ShippingRequest request) {
        log.info("Creating shipping for order: {}", request.orderId());

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + request.orderId()));

        Shipping shipping = new Shipping();
        shipping.setOrder(order);
        shipping.setProvider(request.provider());
        shipping.setAddress(request.address());
        shipping.setStatus(ShippingStatus.PENDING);
        shipping.setCost(request.cost());

        // Save pickup point information
        if (request.pickupPointId() != null) {
            shipping.setPickupPointId(request.pickupPointId());
            shipping.setPickupPointName(request.pickupPointName());
            shipping.setPickupPointAddress(request.pickupPointAddress());
        }

        // Integrate with shipping provider using factory pattern
        try {
            ShippingGateway gateway = gatewayFactory.getGateway(request.provider());
            
            if (gateway.isEnabled()) {
                log.info("Creating shipment with provider: {}", request.provider());
                shipping = gateway.createShipment(order, shipping);
            } else {
                log.info("{} integration disabled - using mock tracking number", request.provider());
                shipping.setTrackingNumber(generateFallbackTrackingNumber());
                shipping.setEstimatedDelivery(LocalDateTime.now().plusDays(4));
                shipping.setStatus(ShippingStatus.PENDING);
            }
        } catch (IllegalArgumentException e) {
            // Provider not supported - use fallback
            log.warn("Shipping provider not supported: {} - using fallback", request.provider());
            shipping.setTrackingNumber(generateFallbackTrackingNumber());
            shipping.setEstimatedDelivery(LocalDateTime.now().plusDays(5));
            shipping.setStatus(ShippingStatus.PENDING);
        }

        shipping = shippingRepository.persist(shipping);
        log.info("Shipping created: {} with tracking: {}", shipping.getUuid(), shipping.getTrackingNumber());

        return shippingMapper.toDto(shipping);
    }

    @Transactional
    public ShippingResponse updateShippingStatus(UUID shippingId, ShippingStatus status) {
        log.info("Updating shipping status: {} -> {}", shippingId, status);

        Shipping shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new RuntimeException("Shipping not found: " + shippingId));

        shipping.setStatus(status);
        shipping = shippingRepository.persist(shipping);

        log.info("Shipping status updated successfully");
        return shippingMapper.toDto(shipping);
    }

    @Transactional
    public ShippingResponse updateShippingStatusByTracking(String trackingNumber, ShippingStatus status) {
        log.info("Updating shipping status by tracking: {} -> {}", trackingNumber, status);

        Shipping shipping = shippingRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipping not found with tracking: " + trackingNumber));

        shipping.setStatus(status);
        shipping = shippingRepository.persist(shipping);

        log.info("Shipping status updated successfully");
        return shippingMapper.toDto(shipping);
    }

    @Transactional(readOnly = true)
    public ShippingResponse getShippingByOrderId(UUID orderUuid) {
        log.info("Fetching shipping for order: {}", orderUuid);

        Shipping shipping = shippingRepository.findByOrderUuid(orderUuid)
                .orElseThrow(() -> new RuntimeException("Shipping not found for order: " + orderUuid));

        return shippingMapper.toDto(shipping);
    }

    @Transactional(readOnly = true)
    public ShippingResponse getShippingByTracking(String trackingNumber) {
        log.info("Fetching shipping by tracking: {}", trackingNumber);

        Shipping shipping = shippingRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipping not found with tracking: " + trackingNumber));

        return shippingMapper.toDto(shipping);
    }

    @Transactional(readOnly = true)
    public byte[] getShippingLabel(UUID orderUuid) {
        log.info("Fetching shipping label for order: {}", orderUuid);

        Shipping shipping = shippingRepository.findByOrderUuid(orderUuid)
                .orElseThrow(() -> new RuntimeException("Shipping not found for order: " + orderUuid));

        if (shipping.getTrackingNumber() == null) {
            throw new RuntimeException("No tracking number available for shipping");
        }

        try {
            ShippingGateway gateway = gatewayFactory.getGateway(shipping.getProvider());
            return gateway.getShippingLabel(shipping.getTrackingNumber());
        } catch (Exception e) {
            log.error("Failed to fetch shipping label: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch shipping label: " + e.getMessage(), e);
        }
    }

    /**
     * Generate fallback tracking number when API fails
     */
    private String generateFallbackTrackingNumber() {
        return "TRACK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
    
    /**
     * Get list of supported countries with their zones
     */
    @Transactional(readOnly = true)
    public List<CountryDto> getSupportedCountries() {
        log.info("Fetching list of supported countries");
        
        List<CountryDto> countries = new ArrayList<>();
        
        // Zone 1: Central Europe
        countries.add(new CountryDto("CZ", "Czech Republic", "🇨🇿", "zone1"));
        countries.add(new CountryDto("SK", "Slovakia", "🇸🇰", "zone1"));
        countries.add(new CountryDto("PL", "Poland", "🇵🇱", "zone1"));
        countries.add(new CountryDto("HU", "Hungary", "🇭🇺", "zone1"));
        countries.add(new CountryDto("AT", "Austria", "🇦🇹", "zone1"));
        countries.add(new CountryDto("RO", "Romania", "🇷🇴", "zone1"));
        
        // Zone 2: Western Europe
        countries.add(new CountryDto("DE", "Germany", "🇩🇪", "zone2"));
        countries.add(new CountryDto("FR", "France", "🇫🇷", "zone2"));
        countries.add(new CountryDto("BE", "Belgium", "🇧🇪", "zone2"));
        countries.add(new CountryDto("NL", "Netherlands", "🇳🇱", "zone2"));
        countries.add(new CountryDto("LU", "Luxembourg", "🇱🇺", "zone2"));
        countries.add(new CountryDto("SI", "Slovenia", "🇸🇮", "zone2"));
        countries.add(new CountryDto("HR", "Croatia", "🇭🇷", "zone2"));
        
        // Zone 3: Rest of Europe
        countries.add(new CountryDto("IT", "Italy", "🇮🇹", "zone3"));
        countries.add(new CountryDto("ES", "Spain", "🇪🇸", "zone3"));
        countries.add(new CountryDto("PT", "Portugal", "🇵🇹", "zone3"));
        countries.add(new CountryDto("GR", "Greece", "🇬🇷", "zone3"));
        countries.add(new CountryDto("BG", "Bulgaria", "🇧🇬", "zone3"));
        countries.add(new CountryDto("EE", "Estonia", "🇪🇪", "zone3"));
        countries.add(new CountryDto("LV", "Latvia", "🇱🇻", "zone3"));
        countries.add(new CountryDto("LT", "Lithuania", "🇱🇹", "zone3"));
        countries.add(new CountryDto("DK", "Denmark", "🇩🇰", "zone3"));
        countries.add(new CountryDto("SE", "Sweden", "🇸🇪", "zone3"));
        countries.add(new CountryDto("FI", "Finland", "🇫🇮", "zone3"));
        countries.add(new CountryDto("NO", "Norway", "🇳🇴", "zone3"));
        countries.add(new CountryDto("IE", "Ireland", "🇮🇪", "zone3"));
        countries.add(new CountryDto("CY", "Cyprus", "🇨🇾", "zone3"));
        countries.add(new CountryDto("MT", "Malta", "🇲🇹", "zone3"));
        
        log.info("Returning {} supported countries", countries.size());
        return countries;
    }
}
