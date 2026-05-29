package com.everage.eshop.controller;

import com.everage.eshop.dto.CountryDto;
import com.everage.eshop.dto.ShippingOptionsResponse;
import com.everage.eshop.dto.ShippingResponse;
import com.everage.eshop.service.shipping.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shipping", description = "Public shipping API")
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping("/options")
    @Operation(summary = "Get available shipping methods and their costs")
    public ShippingOptionsResponse getShippingOptions(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) BigDecimal orderTotal
    ) {
        log.info("Fetching shipping options for country: {}, orderTotal: {}", country, orderTotal);
        return shippingService.getShippingOptions(country, orderTotal);
    }

    @GetMapping("/countries")
    @Operation(summary = "Get list of supported countries")
    public List<CountryDto> getSupportedCountries() {
        log.info("Fetching list of supported countries");
        return shippingService.getSupportedCountries();
    }

    @GetMapping("/track/{trackingNumber}")
    @Operation(summary = "Track shipment by tracking number")
    public ShippingResponse trackShipment(@PathVariable String trackingNumber) {
        log.info("Tracking shipment: {}", trackingNumber);
        return shippingService.getShippingByTracking(trackingNumber);
    }
}
