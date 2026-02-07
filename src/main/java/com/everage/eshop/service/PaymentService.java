package com.everage.eshop.service;

import com.everage.eshop.dto.PaymentRequest;
import com.everage.eshop.dto.PaymentResponse;
import com.everage.eshop.dto.mapper.PaymentMapper;
import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.Payment;
import com.everage.eshop.entity.PaymentStatus;
import com.everage.eshop.exception.OrderNotFoundException;
import com.everage.eshop.repository.OrderRepository;
import com.everage.eshop.repository.PaymentRepository;
import com.everage.eshop.service.gateway.StripePaymentGateway;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final StripePaymentGateway stripePaymentGateway;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}", request.orderId());

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + request.orderId()));

        // Create payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setMethod(request.method());
        payment.setStatus(PaymentStatus.PENDING);

        try {
            // Create PaymentIntent with Stripe
            PaymentIntent paymentIntent = stripePaymentGateway.createPaymentIntent(
                    order.getUuid().toString(),
                    order.getTotalAmount(),
                    order.getEmail()
            );

            payment.setPaymentReference(paymentIntent.getId());

            // For now, we assume payment token is valid and process it
            // In real implementation with client_secret, frontend would handle confirmation
            if ("test".equalsIgnoreCase(request.paymentToken()) ||
                request.paymentToken().startsWith("tok_") ||
                request.paymentToken().startsWith("pm_")) {

                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setGatewayResponse("{\"status\": \"succeeded\", \"paymentIntentId\": \"" +
                    paymentIntent.getId() + "\"}");
                log.info("Payment processed successfully via Stripe: {}", paymentIntent.getId());
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setGatewayResponse("{\"status\": \"failed\", \"error\": \"Invalid token\"}");
                log.warn("Payment failed: invalid token for order {}", order.getUuid());
            }

        } catch (Exception e) {
            log.error("Stripe payment processing error: {}", e.getMessage());
            payment.setStatus(PaymentStatus.FAILED);
            payment.setGatewayResponse("{\"status\": \"failed\", \"error\": \"" + e.getMessage() + "\"}");
            payment.setPaymentReference("ERROR-" + UUID.randomUUID());
        }

        payment = paymentRepository.persist(payment);

        log.info("Payment record saved: {}", payment.getUuid());

        return paymentMapper.toDto(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(UUID paymentId) {
        log.info("Refunding payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Only completed payments can be refunded");
        }

        try {
            // Call Stripe to refund the payment
            stripePaymentGateway.refundPaymentIntent(payment.getPaymentReference());
            payment.setStatus(PaymentStatus.REFUNDED);
            log.info("Payment refunded via Stripe: {}", paymentId);

        } catch (Exception e) {
            log.error("Stripe refund error: {}", e.getMessage());
            throw new RuntimeException("Refund failed: " + e.getMessage(), e);
        }

        payment = paymentRepository.persist(payment);

        log.info("Payment refunded: {}", paymentId);

        return paymentMapper.toDto(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(UUID orderUuid) {
        log.info("Fetching payment for order: {}", orderUuid);

        Payment payment = paymentRepository.findByOrderUuid(orderUuid)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderUuid));

        return paymentMapper.toDto(payment);
    }
}
