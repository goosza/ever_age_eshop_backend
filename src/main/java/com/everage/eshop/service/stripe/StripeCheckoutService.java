package com.everage.eshop.service.stripe;

import com.everage.eshop.dto.CheckoutSessionRequest;
import com.everage.eshop.entity.Item;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.exception.payment.PaymentAmountTooSmallException;
import com.everage.eshop.repository.ItemRepository;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripeCheckoutService {

    private final StripePaymentGateway stripePaymentGateway;
    private final ItemRepository itemRepository;

    // Minimum amount in cents for EUR (€0.50 = 50 cents)
    private static final long MINIMUM_AMOUNT_EUR_CENTS = 50;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Transactional(readOnly = true)
    public String createCheckoutSession(CheckoutSessionRequest request) {
        log.info("Creating Stripe Checkout Session for: {}", request.customerInfo().email());

        // Validate shipping method and required fields
        validateCheckoutRequest(request);

        // Validate items and build line items
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        List<String> productIds = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        long totalAmount = 0;

        for (CheckoutSessionRequest.CheckoutItem checkoutItem : request.items()) {
            UUID productId = UUID.fromString(checkoutItem.productId());
            Item item = itemRepository.findById(productId)
                    .orElseThrow(() -> new ItemNotFoundException("Item not found: " + productId));

            // Verify stock
            if (item.getQuantity() < checkoutItem.quantity()) {
                throw new RuntimeException("Insufficient stock for item: " + item.getName());
            }

            // Calculate total
            totalAmount += checkoutItem.price() * checkoutItem.quantity();

            // Build Stripe line item
            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("eur")
                                    .setUnitAmount(checkoutItem.price()) // price in cents
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName(item.getName())
                                                    .setDescription(item.getDescription())
                                                    .build()
                                    )
                                    .build()
                    )
                    .setQuantity(checkoutItem.quantity().longValue())
                    .build();

            lineItems.add(lineItem);
            
            // Store for metadata
            productIds.add(checkoutItem.productId());
            quantities.add(checkoutItem.quantity());
        }

        // Validate minimum amount
        if (totalAmount < MINIMUM_AMOUNT_EUR_CENTS) {
            log.warn("Order amount too small: {} cents (minimum: {} cents)", totalAmount, MINIMUM_AMOUNT_EUR_CENTS);
            throw new PaymentAmountTooSmallException(
                String.format("Order amount (€%.2f) is below minimum. Minimum order amount is €0.50.", 
                    totalAmount / 100.0)
            );
        }

        // Create Checkout Session - redirect to frontend
        String successUrl = frontendUrl + "/checkout/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = frontendUrl + "/checkout/cancel";

        Session session = stripePaymentGateway.createCheckoutSession(
                lineItems,
                request.customerInfo().email(),
                successUrl,
                cancelUrl,
                request.customerInfo(),
                request.shippingInfo(),
                productIds,
                quantities
        );

        log.info("Checkout Session created: {}", session.getId());
        return session.getUrl();
    }

    /**
     * Validate checkout request based on shipping method
     */
    private void validateCheckoutRequest(CheckoutSessionRequest request) {
        CheckoutSessionRequest.CustomerInfo customer = request.customerInfo();
        CheckoutSessionRequest.ShippingInfo shipping = request.shippingInfo();

        // Basic validation
        if (customer.firstName() == null || customer.firstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (customer.email() == null || customer.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (customer.country() == null || customer.country().isBlank()) {
            throw new IllegalArgumentException("Country is required");
        }

        // Shipping method validation
        if (shipping == null || shipping.method() == null) {
            throw new IllegalArgumentException("Shipping method is required");
        }

        String method = shipping.method();

        // Validation for HOME delivery
        if ("HOME".equals(method)) {
            if (customer.address() == null || customer.address().isBlank()) {
                throw new IllegalArgumentException("Address is required for home delivery");
            }
            if (customer.city() == null || customer.city().isBlank()) {
                throw new IllegalArgumentException("City is required for home delivery");
            }
            if (customer.postalCode() == null || customer.postalCode().isBlank()) {
                throw new IllegalArgumentException("Postal code is required for home delivery");
            }
        }

        // Validation for pickup methods (PICKUP, ZBOX, CARRIER_PICKUP)
        if ("PICKUP".equals(method) || "ZBOX".equals(method) || "CARRIER_PICKUP".equals(method)) {
            if (shipping.pickupPointId() == null || shipping.pickupPointId().isBlank()) {
                throw new IllegalArgumentException("Pickup point is required for " + method + " delivery");
            }
        }

        log.debug("Checkout request validation passed for method: {}", method);
    }
}
