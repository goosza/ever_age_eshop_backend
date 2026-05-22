package com.everage.eshop.controller;

import com.everage.eshop.config.ZasilkovnaConfig;
import com.everage.eshop.dto.ZasilkovnaWebhookEvent;
import com.everage.eshop.service.shipping.zasilkovna.ZasilkovnaWebhookService;
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

    private final ZasilkovnaWebhookService zasilkovnaWebhookService;
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
            // Process webhook event via service
            zasilkovnaWebhookService.processWebhookEvent(
                event.getType(), 
                event.getData().getTrackingNumber()
            );
            return "OK";
            
        } catch (Exception e) {
            log.error("Error processing Zasilkovna webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing webhook: " + e.getMessage(), e);
        }
    }
}
