package com.everage.eshop.service.stripe;

import com.everage.eshop.dto.CheckoutRequest;
import com.everage.eshop.dto.OrderItemRequest;
import com.everage.eshop.dto.ShippingRequest;
import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.OrderStatus;
import com.everage.eshop.entity.Payment;
import com.everage.eshop.entity.PaymentMethod;
import com.everage.eshop.entity.PaymentStatus;
import com.everage.eshop.entity.ShippingProvider;
import com.everage.eshop.repository.OrderRepository;
import com.everage.eshop.repository.PaymentRepository;
import com.everage.eshop.service.OrderService;
import com.everage.eshop.service.shipping.ShippingService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripeWebhookService {

    private final OrderService orderService;
    private final ShippingService shippingService;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void handleCheckoutSessionCompleted(Event event) {
        // Deserialize the session from the event data
        Session session;
        try {
            session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Failed to deserialize session"));
        } catch (Exception e) {
            log.error("Failed to deserialize session: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to deserialize session", e);
        }

        log.info("Checkout session completed: {}", session.getId());
        
        // Validate session data
        if (session.getCustomerDetails() == null || session.getCustomerDetails().getEmail() == null) {
            log.error("Session missing customer details: {}", session.getId());
            throw new RuntimeException("Session missing customer details");
        }
        
        log.info("Customer email: {}", session.getCustomerDetails().getEmail());
        log.info("Amount total: {}", session.getAmountTotal());

        // Extract customer info from session
        String email = session.getCustomerDetails().getEmail();
        String fullName = session.getCustomerDetails().getName();
        
        // Parse name
        String firstName = "Customer";
        String lastName = "";
        if (fullName != null && !fullName.isEmpty()) {
            String[] nameParts = fullName.split(" ", 2);
            firstName = nameParts[0];
            lastName = nameParts.length > 1 ? nameParts[1] : "";
        }

        // Extract metadata (we'll store cart items there)
        Map<String, String> metadata = session.getMetadata();
        
        // For now, we'll need to reconstruct the order from session line items
        // In production, you might want to store order data in session metadata
        
        // Create order
        CheckoutRequest checkoutRequest = buildCheckoutRequestFromSession(session, firstName, lastName, email);
        var orderDto = orderService.createOrder(checkoutRequest);
        
        log.info("Order created: {}", orderDto.orderNumber());

        // Get the created order
        Order order = orderRepository.findById(orderDto.id())
                .orElseThrow(() -> new RuntimeException("Order not found after creation"));

        // Create payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(BigDecimal.valueOf(session.getAmountTotal()).divide(BigDecimal.valueOf(100))); // Convert from cents
        payment.setMethod(PaymentMethod.CREDIT_CARD); // Stripe Checkout uses cards
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentReference(session.getPaymentIntent()); // Stripe PaymentIntent ID
        payment.setGatewayResponse(session.getId()); // Store session ID
        
        paymentRepository.persist(payment);
        log.info("Payment record created for order: {}", order.getOrderNumber());

        // Update order status to CONFIRMED
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.persist(order);
        log.info("Order status updated to CONFIRMED");

        // Create shipping
        ShippingRequest shippingRequest = new ShippingRequest(
                order.getUuid(),
                ShippingProvider.ZASILKOVNA, // Default provider, можно добавить в metadata
                order.getAddress() + ", " + order.getCity() + ", " + order.getPostalCode() + ", " + order.getCountry()
        );
        
        var shippingDto = shippingService.createShipping(shippingRequest);
        log.info("Shipping created with tracking: {}", shippingDto.trackingNumber());

        log.info("Checkout session processing completed for order: {}", order.getOrderNumber());
    }

    @Transactional
    public void handleCheckoutSessionExpired(Event event) {
        // Deserialize the session from the event data
        Session session;
        try {
            session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Failed to deserialize session"));
        } catch (Exception e) {
            log.error("Failed to deserialize session: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to deserialize session", e);
        }

        log.info("Checkout session expired: {}", session.getId());
        log.info("Customer email: {}", session.getCustomerDetails().getEmail());
        
        // Optionally: send email to customer about expired session
        // Optionally: restore cart items to inventory if they were reserved
    }

    private CheckoutRequest buildCheckoutRequestFromSession(Session session, String firstName, String lastName, String email) {
        Map<String, String> metadata = session.getMetadata();
        
        // Extract customer info from metadata (collected on frontend)
        String phone = metadata.getOrDefault("customer_phone", "");
        String addressLine = metadata.getOrDefault("customer_address", "");
        String city = metadata.getOrDefault("customer_city", "");
        String postalCode = metadata.getOrDefault("customer_postal_code", "");
        String country = metadata.getOrDefault("customer_country", "");

        // Extract cart items from metadata
        List<OrderItemRequest> items = new ArrayList<>();
        int itemsCount = Integer.parseInt(metadata.getOrDefault("items_count", "0"));
        
        for (int i = 0; i < itemsCount; i++) {
            String productId = metadata.get("product_id_" + i);
            Integer quantity = Integer.parseInt(metadata.get("quantity_" + i));
            
            items.add(new OrderItemRequest(UUID.fromString(productId), quantity));
        }

        log.info("Extracted {} items from session metadata", items.size());

        return new CheckoutRequest(
                firstName,
                lastName,
                email,
                phone,
                addressLine,
                city,
                postalCode,
                country,
                items,
                null // customer notes
        );
    }
}
