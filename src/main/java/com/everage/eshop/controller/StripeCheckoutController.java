package com.everage.eshop.controller;

import com.everage.eshop.dto.CheckoutSessionRequest;
import com.everage.eshop.dto.CheckoutSessionResponse;
import com.everage.eshop.service.StripeCheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Stripe Checkout", description = "Stripe Checkout Session API")
public class StripeCheckoutController {

    private final StripeCheckoutService stripeCheckoutService;

    @PostMapping("/checkout")
    @Operation(
            summary = "Create Stripe Checkout Session",
            description = "Creates a Stripe Checkout Session and returns the URL for redirect"
    )
    public CheckoutSessionResponse createCheckoutSession(@RequestBody CheckoutSessionRequest request) {
        log.info("Creating checkout session for customer: {}", request.customerInfo().email());
        String sessionUrl = stripeCheckoutService.createCheckoutSession(request);
        return new CheckoutSessionResponse(sessionUrl);
    }
}
