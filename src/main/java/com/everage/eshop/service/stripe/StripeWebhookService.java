package com.everage.eshop.service.stripe;

import com.everage.eshop.dto.CreateOrderRequest;
import com.everage.eshop.dto.OrderDto;
import com.everage.eshop.dto.OrderItemRequest;
import com.everage.eshop.dto.ShippingRequest;
import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.OrderStatus;
import com.everage.eshop.entity.Payment;
import com.everage.eshop.entity.PaymentMethod;
import com.everage.eshop.entity.PaymentStatus;
import com.everage.eshop.entity.ShippingMethod;
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
        
        // Idempotency check - skip if order already exists for this session
        if (orderRepository.findByStripeSessionId(session.getId()).isPresent()) {
            log.warn("Order already exists for session: {} - skipping duplicate webhook", session.getId());
            return;
        }
        
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
        var orderDto = createOrderFromSession(session, firstName, lastName, email);
        
        log.info("Order created: {}", orderDto.orderNumber());

        // Get the created order by UUID
        Order order = orderRepository.findByUuid(orderDto.uuid())
                .orElseThrow(() -> new RuntimeException("Order not found after creation"));

        // Save Stripe session ID
        order.setStripeSessionId(session.getId());
        orderRepository.persist(order);
        log.info("Stripe session ID saved: {}", session.getId());

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
        String shippingMethod = metadata.getOrDefault("shipping_method", ShippingMethod.PICKUP.name());
        
        // Address fields from metadata - only set for HOME delivery
        String deliveryAddress = ShippingMethod.HOME.name().equals(shippingMethod)
                ? metadata.get("customer_address") : null;
        String deliveryCity = ShippingMethod.HOME.name().equals(shippingMethod)
                ? metadata.get("customer_city") : null;
        String deliveryPostalCode = ShippingMethod.HOME.name().equals(shippingMethod)
                ? metadata.get("customer_postal_code") : null;
        String deliveryCountry = metadata.getOrDefault("customer_country", "");

        ShippingRequest shippingRequest = new ShippingRequest(
                order.getUuid(),
                ShippingProvider.ZASILKOVNA,
                new BigDecimal(metadata.getOrDefault("shipping_cost", "12.00")),
                deliveryAddress,
                deliveryCity,
                deliveryPostalCode,
                deliveryCountry,
                metadata.get("pickup_point_id"),
                metadata.get("pickup_point_name"),
                metadata.get("pickup_point_address")
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

    private OrderDto createOrderFromSession(Session session, String firstName, String lastName, String email) {
        Map<String, String> metadata = session.getMetadata();
        
        // Extract customer info from metadata (collected on frontend)
        String phone = metadata.getOrDefault("customer_phone", "");
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

        // Create order request - no address here, it belongs to Shipping
        CreateOrderRequest request = new CreateOrderRequest(
                firstName,
                lastName,
                email,
                phone,
                items,
                null // customer notes
        );

        return orderService.createOrder(request);
    }
}
