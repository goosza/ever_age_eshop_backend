package com.everage.eshop.service;

//import com.everage.eshop.dto.;
import com.everage.eshop.dto.AdminNoteRequest;
import com.everage.eshop.dto.OrderDTO;
import com.everage.eshop.entity.*;
import com.everage.eshop.exception.OrderNotFoundException;
import com.everage.eshop.dto.OrderMapper;
import com.everage.eshop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders(OrderStatus status, String searchQuery, Pageable pageable) {
        Specification<Order> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (searchQuery != null && !searchQuery.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("orderNumber")), "%" + searchQuery.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("email")), "%" + searchQuery.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("firstName")), "%" + searchQuery.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("lastName")), "%" + searchQuery.toLowerCase() + "%")
            ));
        }

        Page<Order> orders = orderRepository.findAll(spec, pageable);
        return orders.map(orderMapper::toDto);
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDTO confirmOrder(UUID orderId, AdminNoteRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order cannot be confirmed in current status");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());

        if (request != null && request.notes() != null) {
            order.setAdminNotes(request.notes());
        }

        addStatusHistory(order, oldStatus, OrderStatus.CONFIRMED, "ADMIN");

        order = orderRepository.persist(order);

        emailService.sendOrderConfirmedEmail(order);

        log.info("Order {} confirmed", order.getOrderNumber());

        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDTO shipOrder(UUID orderId, ShipOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PROCESSING) {
            throw new RuntimeException("Order cannot be shipped in current status");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.SHIPPED);
        order.setShippedAt(LocalDateTime.now());
        order.setTrackingNumber(request.trackingNumber());
        order.setShippingProvider(request.shippingProvider());
        order.generateTrackingUrl();

        if (request.adminNotes() != null) {
            order.setAdminNotes(request.adminNotes());
        }

        addStatusHistory(order, oldStatus, OrderStatus.SHIPPED, "ADMIN");

        order = orderRepository.persist(order);

        emailService.sendOrderShippedEmail(order);

        log.info("Order {} shipped with tracking number: {}", order.getOrderNumber(), request.trackingNumber());

        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDTO deliverOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("Order must be shipped before marking as delivered");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());

        addStatusHistory(order, oldStatus, OrderStatus.DELIVERED, "ADMIN");

        order = orderRepository.persist(order);

        emailService.sendOrderDeliveredEmail(order);

        log.info("Order {} marked as delivered", order.getOrderNumber());

        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDTO cancelOrder(Long orderId, AdminNoteRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Delivered orders cannot be cancelled");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);

        if (request != null && request.notes() != null) {
            order.setAdminNotes(request.notes());
        }

        addStatusHistory(order, oldStatus, OrderStatus.CANCELLED, "ADMIN");

        order = orderRepository.save(order);

        emailService.sendOrderCancelledEmail(order);

        log.info("Order {} cancelled", order.getOrderNumber());

        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDTO updateStatus(Long orderId, UpdateStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.status());

        switch (request.status()) {
            case CONFIRMED -> order.setConfirmedAt(LocalDateTime.now());
            case SHIPPED -> order.setShippedAt(LocalDateTime.now());
            case DELIVERED -> order.setDeliveredAt(LocalDateTime.now());
        }

        addStatusHistory(order, oldStatus, request.status(), request.changedBy() != null ? request.changedBy() : "ADMIN");

        order = orderRepository.persist(order);

        sendStatusChangeEmail(order, request.status());

        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDTO addAdminNote(Long orderId, AdminNoteRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        String currentNotes = order.getAdminNotes() != null ? order.getAdminNotes() : "";
        String newNote = LocalDateTime.now() + ": " + request.notes();
        order.setAdminNotes(currentNotes.isEmpty() ? newNote : currentNotes + "\n" + newNote);

        order = orderRepository.save(order);

        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public OrderStatsDTO getOrderStats() {
        Long totalOrders = orderRepository.count();
        Long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        Long confirmedOrders = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        Long shippedOrders = orderRepository.countByStatus(OrderStatus.SHIPPED);
        Long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        Long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);

        BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatus(OrderStatus.DELIVERED);
        BigDecimal todayRevenue = orderRepository.sumTotalAmountByCreatedAtAfter(
                LocalDateTime.now().toLocalDate().atStartOfDay()
        );

        return new OrderStatsDTO(
                totalOrders,
                pendingOrders,
                confirmedOrders,
                shippedOrders,
                deliveredOrders,
                cancelledOrders,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                todayRevenue != null ? todayRevenue : BigDecimal.ZERO
        );
    }

    // ============================================
    // Helper Methods
    // ============================================
    private void addStatusHistory(Order order, OrderStatus oldStatus, OrderStatus newStatus, String changedBy) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        order.addStatusHistory(history);
    }

    private void sendStatusChangeEmail(Order order, OrderStatus newStatus) {
        switch (newStatus) {
            case CONFIRMED -> emailService.sendOrderConfirmedEmail(order);
            case SHIPPED -> emailService.sendOrderShippedEmail(order);
            case DELIVERED -> emailService.sendOrderDeliveredEmail(order);
            case CANCELLED -> emailService.sendOrderCancelledEmail(order);
        }
    }
}