package com.everage.eshop.controller;

import com.everage.eshop.config.ZasilkovnaConfig;
import com.everage.eshop.dto.ZasilkovnaWebhookEvent;
import com.everage.eshop.entity.ShippingStatus;
import com.everage.eshop.service.shipping.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/zasilkovna")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Zasilkovna Webhook", description = "Webhook endpoint for Zasilkovna status updates")
public class ZasilkovnaWebhookController {

    private final ShippingService shippingService;
    private final ZasilkovnaConfig zasilkovnaConfig;

    @PostMapping("/webhook")
    @Operation(summary = "Receive webhook events from Zasilkovna")
    public String handleWebhook(
            @RequestBody ZasilkovnaWebhookEvent event,
            @RequestHeader(value = "X-Zasilkovna-Signature", required = false) String signature
    ) {
        log.info("Received Zasilkovna webhook: type={}, trackingNumber={}", 
                event.getType(), event.getData().getTrackingNumber());

        // Verify signature (basic check)
        if (signature == null || signature.isEmpty()) {
            log.warn("Webhook received without signature");
            // In production, you might want to reject unsigned webhooks
            // throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing signature");
        }

        try {
            // Process webhook event
            processWebhookEvent(event);
            return "OK";
            
        } catch (Exception e) {
            log.error("Error processing Zasilkovna webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing webhook: " + e.getMessage(), e);
        }
    }

    private void processWebhookEvent(ZasilkovnaWebhookEvent event) {
        String trackingNumber = event.getData().getTrackingNumber();
        
        switch (event.getType()) {
            case "shipment.created":
                log.info("Shipment created: {}", trackingNumber);
                shippingService.updateShippingStatusByTracking(trackingNumber, ShippingStatus.CREATED);
                break;
                
            case "shipment.in_transit":
                log.info("Shipment in transit: {}", trackingNumber);
                shippingService.updateShippingStatusByTracking(trackingNumber, ShippingStatus.IN_TRANSIT);
                // TODO: Send email notification to customer
                break;
                
            case "shipment.ready_for_pickup":
                log.info("Shipment ready for pickup: {}", trackingNumber);
                shippingService.updateShippingStatusByTracking(trackingNumber, ShippingStatus.READY_FOR_PICKUP);
                // TODO: Send email notification to customer with pickup point details
                break;
                
            case "shipment.delivered":
                log.info("Shipment delivered: {}", trackingNumber);
                shippingService.updateShippingStatusByTracking(trackingNumber, ShippingStatus.DELIVERED);
                // TODO: Update order status to DELIVERED
                // TODO: Send thank you email to customer
                break;
                
            case "shipment.returned":
                log.info("Shipment returned: {}", trackingNumber);
                shippingService.updateShippingStatusByTracking(trackingNumber, ShippingStatus.RETURNED);
                // TODO: Send notification email to customer and admin
                break;
                
            default:
                log.warn("Unknown webhook event type: {}", event.getType());
        }
    }
}
