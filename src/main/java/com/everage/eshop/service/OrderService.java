package com.everage.eshop.service;

import com.everage.eshop.dto.CreateOrderRequest;
import com.everage.eshop.dto.OrderDto;
import com.everage.eshop.dto.OrderItemRequest;
import com.everage.eshop.entity.Item;
import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.OrderItem;
import com.everage.eshop.entity.ItemStatus;
import com.everage.eshop.entity.OrderStatus;
import com.everage.eshop.exception.item.InsufficientStockException;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.dto.mapper.OrderMapper;
import com.everage.eshop.exception.order.OrderNotFoundException;
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
    private final OrderMapper orderMapper;

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        log.info("Creating order for email: {}", request.email());

        // Validate all items exist and have stock
        for (OrderItemRequest itemRequest : request.items()) {
            Item item = itemRepository.findByUuid(itemRequest.uuid())
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
        order.setCustomerNotes(request.customerNotes());
        order.setStatus(OrderStatus.PENDING);

        // Add Order Items
        for (OrderItemRequest itemRequest : request.items()) {
            Item item = itemRepository.findByUuid(itemRequest.uuid())
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

    @Transactional(readOnly = true)
    public OrderDto getOrderByNumber(String orderNumber) {
        log.info("Fetching order by number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
        log.info("Found order: {}", order.getOrderNumber());
        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderByStripeSessionId(String sessionId) {
        log.info("Fetching order by Stripe session ID: {}", sessionId);

        Order order = orderRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for session: " + sessionId));
        
        log.info("Found order: {} for session: {}", order.getOrderNumber(), sessionId);
        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByEmail(String email) {
        log.info("Fetching orders for email: {}", email);
        List<OrderDto> orders = orderMapper.toDtoList(orderRepository.findByEmailOrderByCreatedAtDesc(email));
        log.info("Found {} orders", orders.size());
        return orders;
    }
}
