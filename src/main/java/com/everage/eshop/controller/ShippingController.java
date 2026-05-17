package com.everage.eshop.controller;

import com.everage.eshop.dto.ShippingResponse;
import com.everage.eshop.entity.ShippingStatus;
import com.everage.eshop.service.shipping.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shipping", description = "Shipping management endpoints")
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping("/track/{trackingNumber}")
    @Operation(summary = "Track shipment by tracking number")
    public ResponseEntity<ShippingResponse> trackShipment(@PathVariable String trackingNumber) {
        log.info("Tracking shipment: {}", trackingNumber);
        ShippingResponse shipping = shippingService.getShippingByTracking(trackingNumber);
        return ResponseEntity.ok(shipping);
    }

    @GetMapping("/order/{orderUuid}")
    @Operation(summary = "Get shipping by order UUID")
    public ResponseEntity<ShippingResponse> getShippingByOrder(@PathVariable UUID orderUuid) {
        log.info("Fetching shipping for order: {}", orderUuid);
        ShippingResponse shipping = shippingService.getShippingByOrderId(orderUuid);
        return ResponseEntity.ok(shipping);
    }

    @GetMapping("/order/{orderUuid}/label")
    @Operation(summary = "Download shipping label PDF")
    public ResponseEntity<byte[]> getShippingLabel(@PathVariable UUID orderUuid) {
        log.info("Downloading shipping label for order: {}", orderUuid);
        
        byte[] labelPdf = shippingService.getShippingLabel(orderUuid);
        
        ShippingResponse shipping = shippingService.getShippingByOrderId(orderUuid);
        String filename = "label-" + shipping.trackingNumber() + ".pdf";
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(labelPdf);
    }

    @PatchMapping("/{shippingUuid}/status")
    @Operation(summary = "Update shipping status (Admin)")
    public ResponseEntity<ShippingResponse> updateShippingStatus(
            @PathVariable UUID shippingUuid,
            @RequestParam ShippingStatus status
    ) {
        log.info("Updating shipping status: {} -> {}", shippingUuid, status);
        ShippingResponse shipping = shippingService.updateShippingStatus(shippingUuid, status);
        return ResponseEntity.ok(shipping);
    }
}
