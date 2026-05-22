package com.everage.eshop.service;

import com.everage.eshop.dto.CheckoutSessionRequest;
import com.everage.eshop.entity.Item;
import com.everage.eshop.entity.ItemStatus;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.repository.ItemRepository;
import com.everage.eshop.service.stripe.StripePaymentGateway;
import com.everage.eshop.service.stripe.StripeCheckoutService;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripeCheckoutServiceTest {

    @Mock
    private StripePaymentGateway stripePaymentGateway;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private StripeCheckoutService stripeCheckoutService;

    private UUID itemId;
    private Item item;
    private CheckoutSessionRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stripeCheckoutService, "frontendUrl", "http://localhost:3000");

        itemId = UUID.randomUUID();
        item = new Item();
        item.setUuid(itemId);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setPrice(BigDecimal.valueOf(29.99));
        item.setQuantity(10);
        item.setStatus(ItemStatus.ACTIVE);

        CheckoutSessionRequest.CustomerInfo customerInfo = new CheckoutSessionRequest.CustomerInfo(
                "John",
                "Doe",
                "john@example.com",
                "+1234567890",
                "123 Main St",
                "New York",
                "10001",
                "US"
        );

        CheckoutSessionRequest.CheckoutItem checkoutItem = new CheckoutSessionRequest.CheckoutItem(
                itemId.toString(),
                2,
                2999L // 29.99 EUR in cents
        );

        CheckoutSessionRequest.ShippingInfo shippingInfo = new CheckoutSessionRequest.ShippingInfo(
                "ZASILKOVNA",
                "PICKUP",
                BigDecimal.valueOf(12.00),
                "12345",
                "Zasilkovna Prague",
                "Central Square 1"
        );

        request = new CheckoutSessionRequest(customerInfo, List.of(checkoutItem), shippingInfo);
    }

    @Test
    void createCheckoutSession_ShouldReturnSessionUrl() {
        // Given
        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn("cs_test_123");
        when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_123");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(stripePaymentGateway.createCheckoutSession(
                anyList(),
                eq("john@example.com"),
                anyString(),
                anyString(),
                any(),
                any(),
                anyList(),
                anyList()
        )).thenReturn(mockSession);

        // When
        String sessionUrl = stripeCheckoutService.createCheckoutSession(request);

        // Then
        assertNotNull(sessionUrl);
        assertEquals("https://checkout.stripe.com/pay/cs_test_123", sessionUrl);
        verify(itemRepository).findById(itemId);
        verify(stripePaymentGateway).createCheckoutSession(
                anyList(),
                eq("john@example.com"),
                eq("http://localhost:3000/checkout/success?session_id={CHECKOUT_SESSION_ID}"),
                eq("http://localhost:3000/checkout/cancel"),
                any(),
                any(),
                anyList(),
                anyList()
        );
    }

    @Test
    void createCheckoutSession_WithMultipleItems_ShouldCreateSession() {
        // Given
        UUID itemId2 = UUID.randomUUID();
        Item item2 = new Item();
        item2.setUuid(itemId2);
        item2.setName("Test Item 2");
        item2.setDescription("Test Description 2");
        item2.setPrice(BigDecimal.valueOf(49.99));
        item2.setQuantity(5);
        item2.setStatus(ItemStatus.ACTIVE);

        CheckoutSessionRequest.CheckoutItem checkoutItem2 = new CheckoutSessionRequest.CheckoutItem(
                itemId2.toString(),
                1,
                4999L
        );

        CheckoutSessionRequest multiItemRequest = new CheckoutSessionRequest(
                request.customerInfo(),
                List.of(request.items().get(0), checkoutItem2),
                request.shippingInfo()
        );

        Session mockSession = mock(Session.class);
        when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_456");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.findById(itemId2)).thenReturn(Optional.of(item2));
        when(stripePaymentGateway.createCheckoutSession(anyList(), anyString(), anyString(), anyString(), any(), any(), anyList(), anyList()))
                .thenReturn(mockSession);

        // When
        String sessionUrl = stripeCheckoutService.createCheckoutSession(multiItemRequest);

        // Then
        assertNotNull(sessionUrl);
        verify(itemRepository).findById(itemId);
        verify(itemRepository).findById(itemId2);
    }

    @Test
    void createCheckoutSession_WithNonExistentItem_ShouldThrowException() {
        // Given
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ItemNotFoundException.class, () ->
                stripeCheckoutService.createCheckoutSession(request)
        );
        verify(itemRepository).findById(itemId);
        verify(stripePaymentGateway, never()).createCheckoutSession(anyList(), anyString(), anyString(), anyString(), any(), any(), anyList(), anyList());
    }

    @Test
    void createCheckoutSession_WithInsufficientStock_ShouldThrowException() {
        // Given
        item.setQuantity(1); // Less than requested quantity (2)
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                stripeCheckoutService.createCheckoutSession(request)
        );
        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(itemRepository).findById(itemId);
        verify(stripePaymentGateway, never()).createCheckoutSession(anyList(), anyString(), anyString(), anyString(), any(), any(), anyList(), anyList());
    }

    @Test
    void createCheckoutSession_WithZeroQuantity_ShouldThrowException() {
        // Given
        item.setQuantity(0);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                stripeCheckoutService.createCheckoutSession(request)
        );
        assertTrue(exception.getMessage().contains("Insufficient stock"));
    }

    @Test
    void createCheckoutSession_WithInvalidUUID_ShouldThrowException() {
        // Given
        CheckoutSessionRequest.CheckoutItem invalidItem = new CheckoutSessionRequest.CheckoutItem(
                "invalid-uuid",
                1,
                1000L
        );
        CheckoutSessionRequest invalidRequest = new CheckoutSessionRequest(
                request.customerInfo(),
                List.of(invalidItem),
                request.shippingInfo()
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                stripeCheckoutService.createCheckoutSession(invalidRequest)
        );
        verify(stripePaymentGateway, never()).createCheckoutSession(anyList(), anyString(), anyString(), anyString(), any(), any(), anyList(), anyList());
    }
}
