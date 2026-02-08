package com.everage.eshop.service;

import com.everage.eshop.dto.CheckoutRequest;
import com.everage.eshop.dto.OrderDTO;
import com.everage.eshop.dto.OrderItemRequest;
import com.everage.eshop.dto.PaymentRequest;
import com.everage.eshop.dto.PaymentResponse;
import com.everage.eshop.dto.ShippingRequest;
import com.everage.eshop.dto.ShippingResponse;
import com.everage.eshop.entity.Item;
import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.OrderItem;
import com.everage.eshop.entity.ItemStatus;
import com.everage.eshop.entity.OrderStatus;
import com.everage.eshop.entity.PaymentMethod;
import com.everage.eshop.entity.PaymentStatus;
import com.everage.eshop.entity.ShippingProvider;
import com.everage.eshop.exception.item.InsufficientStockException;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.dto.mapper.OrderMapper;
import com.everage.eshop.exception.OrderNotFoundException;
import com.everage.eshop.repository.ItemRepository;
import com.everage.eshop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final ItemService itemService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderDTO createOrder(CheckoutRequest request) {
        log.info("Creating order for email: {}", request.email());

        // Validate all items exist and have stock
        for (OrderItemRequest itemRequest : request.items()) {
            Item item = itemRepository.findById(itemRequest.uuid())
                    .orElseThrow(() -> new ItemNotFoundException("Item not found: " + itemRequest.uuid()));

            // Check stock availability
            if (item.getQuantity() < itemRequest.quantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for item: " + item.getName() +
                                ". Available: " + item.getQuantity() +
                                ", Requested: " + itemRequest.quantity()
                );
            }

            // Check item status
            if (item.getStatus() == ItemStatus.OUT_OF_STOCK ||
                    item.getStatus() == ItemStatus.INACTIVE) {
                throw new IllegalStateException("Item is not available: " + item.getName());
            }
        }

        // Create Order
        Order order = new Order();
        order.setFirstName(request.firstName());
        order.setLastName(request.lastName());
        order.setEmail(request.email());
        order.setPhone(request.phone());
        order.setAddress(request.address());
        order.setCity(request.city());
        order.setPostalCode(request.postalCode());
        order.setCountry(request.country());
        order.setCustomerNotes(request.customerNotes());
        order.setStatus(OrderStatus.PENDING);

        // Add Order Items
        for (OrderItemRequest itemRequest : request.items()) {
            Item item = itemRepository.findById(itemRequest.uuid())
                    .orElseThrow(() -> new ItemNotFoundException("Item not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setPrice(item.getPrice());

            order.addItem(orderItem);

            // Decrease item quantity
            itemService.decreaseQuantity(item.getUuid(), itemRequest.quantity());

            log.info("Added item to order: {} x{}", item.getName(), itemRequest.quantity());
        }

        // Calculate totals
        order.calculateTotals();

        // Save Order
        order = orderRepository.persist(order);

        log.info("Order created successfully: {}, Total: {}",
                order.getOrderNumber(), order.getTotalAmount());

        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDTO completeCheckout(CheckoutRequest checkoutRequest, PaymentMethod paymentMethod,
                                     String paymentToken, ShippingProvider shippingProvider) {
        log.info("Starting complete checkout for email: {}", checkoutRequest.email());

        // Step 1: Create Order
        OrderDTO orderDTO = createOrder(checkoutRequest);
        log.info("Step 1 - Order created: {}", orderDTO.orderNumber());

        Order order = orderRepository.findById(orderDTO.id())
                .orElseThrow(() -> new OrderNotFoundException("Order not found after creation"));

        // Step 2: Process Payment
        PaymentRequest paymentRequest = new PaymentRequest(order.getUuid(), paymentMethod, paymentToken);
        PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);
        log.info("Step 2 - Payment processed: {}", paymentResponse.status());

        if (paymentResponse.status() != PaymentStatus.COMPLETED) {
            // Payment failed - order remains PENDING
            log.error("Payment failed for order: {}", order.getOrderNumber());
            throw new RuntimeException("Payment processing failed: " + paymentResponse.status());
        }

        // Step 3: Update Order Status to CONFIRMED
        order.setStatus(OrderStatus.CONFIRMED);
        order = orderRepository.persist(order);
        log.info("Step 3 - Order status updated to CONFIRMED");

        // Step 4: Create Shipping
        ShippingRequest shippingRequest = new ShippingRequest(
                order.getUuid(),
                shippingProvider,
                order.getAddress() + ", " + order.getCity() + ", " + order.getPostalCode() + ", " + order.getCountry()
        );
        ShippingResponse shippingResponse = shippingService.createShipping(shippingRequest);
        log.info("Step 4 - Shipping created: {}", shippingResponse.trackingNumber());

        log.info("Complete checkout successful for order: {}", order.getOrderNumber());

        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderByNumber(String orderNumber) {
        log.info("Fetching order by number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
        log.info("Found order: {}", order.getOrderNumber());
        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByEmail(String email) {
        log.info("Fetching orders for email: {}", email);
        List<OrderDTO> orders = orderMapper.toDtoList(orderRepository.findByEmailOrderByCreatedAtDesc(email));
        log.info("Found {} orders", orders.size());
        return orders;
    }
}
