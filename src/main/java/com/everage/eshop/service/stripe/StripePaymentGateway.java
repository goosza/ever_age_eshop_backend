package com.everage.eshop.service.stripe;

import com.everage.eshop.dto.CheckoutSessionRequest;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StripePaymentGateway {

    /**
     * Create a Stripe Checkout Session for hosted payment page
     */
    public Session createCheckoutSession(
            List<SessionCreateParams.LineItem> lineItems,
            String customerEmail,
            String successUrl,
            String cancelUrl,
            CheckoutSessionRequest.CustomerInfo customerInfo,
            List<String> productIds,
            List<Integer> quantities
    ) {
        try {
            // Build metadata with cart information
            Map<String, String> metadata = new HashMap<>();
            metadata.put("customer_first_name", customerInfo.firstName());
            metadata.put("customer_last_name", customerInfo.lastName());
            metadata.put("customer_phone", customerInfo.phone() != null ? customerInfo.phone() : "");
            metadata.put("customer_address", customerInfo.address());
            metadata.put("customer_city", customerInfo.city());
            metadata.put("customer_postal_code", customerInfo.postalCode());
            metadata.put("customer_country", customerInfo.country());
            
            // Store cart items (product IDs and quantities)
            for (int i = 0; i < productIds.size(); i++) {
                metadata.put("product_id_" + i, productIds.get(i));
                metadata.put("quantity_" + i, quantities.get(i).toString());
            }
            metadata.put("items_count", String.valueOf(productIds.size()));

            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setCustomerEmail(customerEmail)
                    .addAllLineItem(lineItems)
                    .putAllMetadata(metadata)
                    .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED);
                    // Убрали .setShippingAddressCollection() - адрес уже собран на фронтенде

            SessionCreateParams params = paramsBuilder.build();
            Session session = Session.create(params);

            log.info("Checkout Session created: {} for customer: {}", session.getId(), customerEmail);
            return session;

        } catch (StripeException e) {
            log.error("Failed to create Checkout Session for {}: {}", customerEmail, e.getMessage());
            throw new RuntimeException("Checkout session creation error: " + e.getMessage(), e);
        }
    }
}

