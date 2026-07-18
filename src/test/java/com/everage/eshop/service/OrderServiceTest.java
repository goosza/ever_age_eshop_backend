package com.everage.eshop.service;

import com.everage.eshop.dto.OrderDto;
import com.everage.eshop.dto.mapper.OrderMapper;
import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.OrderStatus;
import com.everage.eshop.exception.order.OrderNotFoundException;
import com.everage.eshop.repository.ItemRepository;
import com.everage.eshop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemService itemService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getOrderByNumber_WhenExists_ShouldReturnDto() {
        Order order = new Order();
        order.setUuid(UUID.randomUUID());
        order.setOrderNumber("EVE-2026-ABCDEFGHJK");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.TEN);

        OrderDto dto = new OrderDto(
                order.getUuid(), order.getOrderNumber(), "John", "Doe", "john@example.com", null,
                java.util.List.of(), BigDecimal.TEN, OrderStatus.PENDING, null, null, null, null, null
        );

        when(orderRepository.findByOrderNumber("EVE-2026-ABCDEFGHJK")).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(dto);

        OrderDto result = orderService.getOrderByNumber("EVE-2026-ABCDEFGHJK");

        assertEquals("EVE-2026-ABCDEFGHJK", result.orderNumber());
        verify(orderRepository).findByOrderNumber("EVE-2026-ABCDEFGHJK");
    }

    @Test
    void getOrderByNumber_WhenNotFound_ShouldThrowOrderNotFoundException() {
        // Regression test: this must be a 404-mapped exception, not a raw RuntimeException
        // that would surface as a 500 and leak internal error details on a public endpoint.
        when(orderRepository.findByOrderNumber("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderByNumber("UNKNOWN"));
    }
}
