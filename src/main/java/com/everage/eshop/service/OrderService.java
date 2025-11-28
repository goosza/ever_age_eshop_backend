package com.everage.eshop.service;

//import com.everage.eshop.dto.;
import com.everage.eshop.dto.CheckoutRequest;
import com.everage.eshop.dto.OrderDTO;
import com.everage.eshop.dto.CartItemRequest;
//import com.everage.eshop.entity.*;
import com.everage.eshop.entity.Item;
import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.OrderItem;
import com.everage.eshop.entity.OrderStatus;
import com.everage.eshop.entity.ItemStatus;
import com.everage.eshop.entity.OrderStatusHistory;
import com.everage.eshop.exception.InsufficientStockException;
import com.everage.eshop.exception.ItemNotFoundException;
import com.everage.eshop.exception.OrderNotFoundException;
import com.everage.eshop.dto.OrderMapper;
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
    private final EmailService emailService;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderDTO createOrder(CheckoutRequest request) {
        log.info("Creating order for email: {}", request.email());

        // Validate all items exist and have stock
        for (CartItemRequest cartItem : request.items()) {
            Item item = itemRepository.findById(cartItem.itemId())
                    .orElseThrow(() -> new ItemNotFoundException("Item not found: " + cartItem.itemId()));

            // Check stock availability
            if (item.getQuantity() < cartItem.quantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for item: " + item.getName() +
                                ". Available: " + item.getQuantity() +
                                ", Requested: " + cartItem.quantity()
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
        order.setBirthDate(request.birthDate());
        order.setAddress(request.address());
        order.setCity(request.city());
        order.setPostalCode(request.postalCode());
        order.setCountry(request.country());
        order.setCustomerNotes(request.customerNotes());

        // Add Order Items
        for (CartItemRequest cartItem : request.items()) {
            Item item = itemRepository.findById(cartItem.itemId())
                    .orElseThrow(() -> new ItemNotFoundException("Item not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setQuantity(cartItem.quantity());
            orderItem.setPrice(item.getPrice());

            order.addItem(orderItem);

            // Decrease item quantity
            itemService.decreaseQuantity(item.getUuid(), cartItem.quantity());

            log.info("Added item to order: {} x{}", item.getName(), cartItem.quantity());
        }

        // Calculate totals
        order.calculateTotals();

        // Add initial status history
        OrderStatusHistory initialHistory = new OrderStatusHistory();
        initialHistory.setOldStatus(null);
        initialHistory.setNewStatus(OrderStatus.PENDING);
        initialHistory.setChangedBy("SYSTEM");
        initialHistory.setNotes("Order created");
        order.addStatusHistory(initialHistory);

        // Save Order
        order = orderRepository.persist(order);

        log.info("Order created successfully: {}, Total: {}",
                order.getOrderNumber(), order.getTotalAmount());

        // Send confirmation email
        try {
            emailService.sendOrderConfirmationEmail(order);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email", e);
        }
        // Map to DTO using MapStruct
        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderByNumber(String orderNumber) {
        log.info("Fetching order by number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
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