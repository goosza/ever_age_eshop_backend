package com.everage.eshop.controller;

import com.everage.eshop.dto.ShippingResponse;
import com.everage.eshop.entity.ShippingStatus;
import com.everage.eshop.service.shipping.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/admin/shipping")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Shipping", description = "Shipping management for administrators")
@SecurityRequirement(name = "hmac-auth")
public class AdminShippingController {

    private final ShippingService shippingService;

    @GetMapping("/order/{orderUuid}/label")
    @Operation(summary = "Download shipping label PDF")
    public ResponseEntity<byte[]> getShippingLabel(@PathVariable UUID orderUuid) {
        log.info("Admin: downloading shipping label for order: {}", orderUuid);

        byte[] labelPdf = shippingService.getShippingLabel(orderUuid);
        ShippingResponse shipping = shippingService.getShippingByOrderId(orderUuid);
        String filename = "label-" + shipping.trackingNumber() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(labelPdf);
    }

    @PatchMapping("/{shippingUuid}/status")
    @Operation(summary = "Update shipping status manually")
    public ShippingResponse updateShippingStatus(
            @PathVariable UUID shippingUuid,
            @RequestParam ShippingStatus status
    ) {
        log.info("Admin: updating shipping status: {} -> {}", shippingUuid, status);
        return shippingService.updateShippingStatus(shippingUuid, status);
    }
}
