package com.everage.eshop.service;

import com.everage.eshop.dto.OrderDto;
import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.OrderStatus;
import com.everage.eshop.exception.order.OrderNotFoundException;
import com.everage.eshop.dto.mapper.OrderMapper;
import com.everage.eshop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;


    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders(OrderStatus status) {
        List<Order> orders = status != null
                ? orderRepository.findByStatus(status)
                : orderRepository.findAllByOrderByCreatedAtDesc();
        log.info("Admin: returning {} orders", orders.size());
        return orderMapper.toDtoList(orders);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(UUID orderId) {
        Order order = orderRepository.findByUuid(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto shipOrder(UUID orderId) {
        Order order = orderRepository.findByUuid(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PROCESSING) {
            throw new RuntimeException("Order cannot be shipped in current status");
        }

        order.setStatus(OrderStatus.SHIPPED);

        order = orderRepository.persist(order);

        log.info("Order {} shipped", order.getOrderNumber());

        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto deliverOrder(UUID orderId) {
        Order order = orderRepository.findByUuid(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("Order must be shipped before marking as delivered");
        }

        order.setStatus(OrderStatus.DELIVERED);

        order = orderRepository.persist(order);


        log.info("Order {} marked as delivered", order.getOrderNumber());

        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto cancelOrder(UUID orderId) {
        Order order = orderRepository.findByUuid(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Delivered orders cannot be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);

        order = orderRepository.persist(order);

        log.info("Order {} cancelled", order.getOrderNumber());

        return orderMapper.toDto(order);
    }
}