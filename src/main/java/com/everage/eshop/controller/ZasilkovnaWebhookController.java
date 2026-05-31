package com.everage.eshop.controller;

import com.everage.eshop.config.ZasilkovnaConfig;
import com.everage.eshop.dto.ZasilkovnaWebhookEvent;
import com.everage.eshop.service.shipping.zasilkovna.ZasilkovnaWebhookService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/zasilkovna")
@RequiredArgsConstructor
@Slf4j
@Hidden // Hide from Swagger UI
public class ZasilkovnaWebhookController {

    private final ZasilkovnaWebhookService zasilkovnaWebhookService;
    private final ZasilkovnaConfig zasilkovnaConfig;

    @Value("${zasilkovna.webhook.secret:}")
    private String webhookSecret;

    /**
     * Zásilkovna does not use cryptographic signatures on webhooks.
     * We protect this endpoint with a secret token in the URL query parameter.
     *
     * Register the webhook URL in Zásilkovna client section as:
     *   https://your-domain.com/api/zasilkovna/webhook?token=YOUR_SECRET
     */
    @PostMapping("/webhook")
    public String handleWebhook(
            @RequestBody ZasilkovnaWebhookEvent event,
            @RequestParam(value = "token", required = false) String token
    ) {
        // Validate secret token
        if (webhookSecret != null && !webhookSecret.isEmpty()) {
            if (token == null || !webhookSecret.equals(token)) {
                log.warn("Zasilkovna webhook received with invalid or missing token");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid webhook token");
            }
        } else {
            log.warn("Zasilkovna webhook secret not configured - accepting all requests (dev only)");
        }

        log.info("Received Zasilkovna webhook: type={}, trackingNumber={}",
                event.getType(), event.getData().getTrackingNumber());

        try {
            zasilkovnaWebhookService.processWebhookEvent(
                event.getType(),
                event.getData().getTrackingNumber()
            );
            return "OK";

        } catch (Exception e) {
            log.error("Error processing Zasilkovna webhook: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Webhook processing failed: " + e.getMessage());
        }
    }
}
