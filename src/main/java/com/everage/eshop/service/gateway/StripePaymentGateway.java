package com.everage.eshop.service.gateway;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class StripePaymentGateway {

    /**
     * Create a PaymentIntent for card payments
     * This is the first step in Stripe's payment flow
     */
    public PaymentIntent createPaymentIntent(String orderId, BigDecimal amount, String customerEmail) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue()) // Convert to cents
                    .setCurrency("eur")
                    .setDescription("Order #" + orderId)
                    .setReceiptEmail(customerEmail)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            log.info("PaymentIntent created: {} for order: {}", paymentIntent.getId(), orderId);
            return paymentIntent;

        } catch (StripeException e) {
            log.error("Failed to create PaymentIntent for order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Payment gateway error: " + e.getMessage(), e);
        }
    }

    /**
     * Confirm payment with token (from Apple Pay, Card, etc)
     * In production, the frontend would handle this with client_secret
     * This is a simplified version for testing
     */
    public PaymentIntent confirmPayment(String paymentIntentId, String paymentToken) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // In production:
            // 1. Frontend gets client_secret from createPaymentIntent
            // 2. Frontend uses Stripe.js to confirm payment with payment method
            // 3. Backend receives webhook confirming payment success
            //
            // For now, we simulate successful confirmation if token is provided
            log.info("Payment confirmation received for PaymentIntent: {}", paymentIntentId);

            if (paymentIntent.getStatus().equals("succeeded")) {
                log.info("PaymentIntent already succeeded: {}", paymentIntentId);
            } else {
                log.info("PaymentIntent status: {}", paymentIntent.getStatus());
            }

            return paymentIntent;

        } catch (StripeException e) {
            log.error("Failed to confirm payment {}: {}", paymentIntentId, e.getMessage());
            throw new RuntimeException("Payment confirmation error: " + e.getMessage(), e);
        }
    }

    /**
     * Refund a PaymentIntent
     * Can only refund if the PaymentIntent has succeeded
     */
    public Refund refundPaymentIntent(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if (!paymentIntent.getStatus().equals("succeeded")) {
                throw new RuntimeException("Cannot refund PaymentIntent with status: " + paymentIntent.getStatus());
            }

            // Create refund for the payment
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();

            Refund refund = Refund.create(params);
            log.info("PaymentIntent {} refunded. Refund ID: {}", paymentIntentId, refund.getId());
            return refund;

        } catch (StripeException e) {
            log.error("Failed to refund PaymentIntent {}: {}", paymentIntentId, e.getMessage());
            throw new RuntimeException("PaymentIntent refund error: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve PaymentIntent status
     * Useful for checking payment status after user confirms
     */
    public PaymentIntent getPaymentIntent(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            log.info("Retrieved PaymentIntent {}: status={}", paymentIntentId, paymentIntent.getStatus());
            return paymentIntent;

        } catch (StripeException e) {
            log.error("Failed to retrieve PaymentIntent {}: {}", paymentIntentId, e.getMessage());
            throw new RuntimeException("Failed to retrieve payment intent: " + e.getMessage(), e);
        }
    }
}
