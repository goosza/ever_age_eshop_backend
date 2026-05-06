package com.everage.eshop.service.stripe;

import com.everage.eshop.dto.CheckoutSessionRequest;
import com.everage.eshop.entity.Item;
import com.everage.eshop.exception.item.ItemNotFoundException;
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

    @Value("${server.url:http://localhost:8080}")
    private String serverUrl;

    @Transactional(readOnly = true)
    public String createCheckoutSession(CheckoutSessionRequest request) {
        log.info("Creating Stripe Checkout Session for: {}", request.customerInfo().email());

        // Validate items and build line items
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        List<String> productIds = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();

        for (CheckoutSessionRequest.CheckoutItem checkoutItem : request.items()) {
            UUID productId = UUID.fromString(checkoutItem.productId());
            Item item = itemRepository.findById(productId)
                    .orElseThrow(() -> new ItemNotFoundException("Item not found: " + productId));

            // Verify stock
            if (item.getQuantity() < checkoutItem.quantity()) {
                throw new RuntimeException("Insufficient stock for item: " + item.getName());
            }

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

        // Create Checkout Session
        String successUrl = serverUrl + "/checkout/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = serverUrl + "/checkout/cancel";

        Session session = stripePaymentGateway.createCheckoutSession(
                lineItems,
                request.customerInfo().email(),
                successUrl,
                cancelUrl,
                request.customerInfo(),
                productIds,
                quantities
        );

        log.info("Checkout Session created: {}", session.getId());
        return session.getUrl();
    }
}
