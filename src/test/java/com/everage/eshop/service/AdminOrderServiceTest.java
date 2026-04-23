package com.everage.eshop.service;

import com.everage.eshop.dto.OrderDto;
import com.everage.eshop.dto.mapper.OrderMapper;
import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.OrderStatus;
import com.everage.eshop.exception.order.OrderNotFoundException;
import com.everage.eshop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private AdminOrderService adminOrderService;

    // ── getOrder ──────────────────────────────────────────────────────────────

    @Test
    void getOrder_WhenExists_ShouldReturnDto() {
        UUID id = UUID.randomUUID();
        Order order = createOrder(OrderStatus.PENDING);
        OrderDto dto = createOrderDto(id, OrderStatus.PENDING);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(dto);

        OrderDto result = adminOrderService.getOrder(id);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.status());
        verify(orderRepository).findById(id);
    }

    @Test
    void getOrder_WhenNotFound_ShouldThrow() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> adminOrderService.getOrder(id));
    }

    // ── confirmOrder ──────────────────────────────────────────────────────────

    @Test
    void confirmOrder_WhenPending_ShouldSetConfirmed() {
        UUID id = UUID.randomUUID();
        Order order = createOrder(OrderStatus.PENDING);
        OrderDto dto = createOrderDto(id, OrderStatus.CONFIRMED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.persist(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);

        OrderDto result = adminOrderService.confirmOrder(id);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(OrderStatus.CONFIRMED, result.status());
        verify(orderRepository).persist(order);
    }

    @Test
    void confirmOrder_WhenNotPending_ShouldThrow() {
        UUID id = UUID.randomUUID();
        Order order = createOrder(OrderStatus.CONFIRMED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> adminOrderService.confirmOrder(id));
        verify(orderRepository, never()).persist(any());
    }

    @Test
    void confirmOrder_WhenNotFound_ShouldThrow() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> adminOrderService.confirmOrder(id));
    }

    // ── shipOrder ─────────────────────────────────────────────────────────────

    @Test
    void shipOrder_WhenConfirmed_ShouldSetShipped() {
        UUID id = UUID.randomUUID();
        Order order = createOrder(OrderStatus.CONFIRMED);
        OrderDto dto = createOrderDto(id, OrderStatus.SHIPPED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.persist(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);

        OrderDto result = adminOrderService.shipOrder(id);

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertEquals(OrderStatus.SHIPPED, result.status());
    }

    @Test
    void shipOrder_WhenProcessing_ShouldSetShipped() {
        UUID id = UUID.randomUUID();
        Order order = createOrder(OrderStatus.PROCESSING);
        OrderDto dto = createOrderDto(id, OrderStatus.SHIPPED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.persist(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);

        adminOrderService.shipOrder(id);

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    void shipOrder_WhenPending_ShouldThrow() {
        UUID id = UUID.randomUUID();
        Order order = createOrder(OrderStatus.PENDING);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> adminOrderService.shipOrder(id));
        verify(orderRepository, never()).persist(any());
    }

    // ── deliverOrder ──────────────────────────────────────────────────────────

    @Test
    void deliverOrder_WhenShipped_ShouldSetDelivered() {
        UUID id = UUID.randomUUID();
        Order order = createOrder(OrderStatus.SHIPPED);
        OrderDto dto = createOrderDto(id, OrderStatus.DELIVERED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.persist(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);

        OrderDto result = adminOrderService.deliverOrder(id);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(OrderStatus.DELIVERED, result.status());
    }

    @Test
    void deliverOrder_WhenNotShipped_ShouldThrow() {
        UUID id = UUID.randomUUID();
        Order order = createOrder(OrderStatus.CONFIRMED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> adminOrderService.deliverOrder(id));
        verify(orderRepository, never()).persist(any());
    }

    // ── cancelOrder ───────────────────────────────────────────────────────────

    @Test
    void cancelOrder_WhenPending_ShouldSetCancelled() {
        UUID id = UUID.randomUUID();
        Order order = createOrder(OrderStatus.PENDING);
        OrderDto dto = createOrderDto(id, OrderStatus.CANCELLED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.persist(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);

        OrderDto result = adminOrderService.cancelOrder(id);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(OrderStatus.CANCELLED, result.status());
    }

    @Test
    void cancelOrder_WhenDelivered_ShouldThrow() {
        UUID id = UUID.randomUUID();
        Order order = createOrder(OrderStatus.DELIVERED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> adminOrderService.cancelOrder(id));
        verify(orderRepository, never()).persist(any());
    }

    @Test
    void cancelOrder_WhenNotFound_ShouldThrow() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> adminOrderService.cancelOrder(id));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Order createOrder(OrderStatus status) {
        Order order = new Order();
        order.setUuid(UUID.randomUUID());
        order.setOrderNumber("EVE-2024-000001");
        order.setFirstName("John");
        order.setLastName("Doe");
        order.setEmail("john@example.com");
        order.setPhone("+420123456789");
        order.setAddress("Main St 1");
        order.setCity("Prague");
        order.setPostalCode("11000");
        order.setCountry("CZ");
        order.setTotalAmount(BigDecimal.valueOf(99.99));
        order.setStatus(status);
        return order;
    }

    private OrderDto createOrderDto(UUID id, OrderStatus status) {
        return new OrderDto(
                id, "EVE-2024-000001",
                "John", "Doe", "john@example.com", "+420123456789",
                "Main St 1", "Prague", "11000", "CZ",
                List.of(), BigDecimal.valueOf(99.99), status,
                null, LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
