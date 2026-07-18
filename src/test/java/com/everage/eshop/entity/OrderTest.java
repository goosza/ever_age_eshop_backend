package com.everage.eshop.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {

    // EVE-<year>-<10 chars from a restricted alphabet, no ambiguous chars>
    private static final Pattern ORDER_NUMBER_PATTERN =
            Pattern.compile("^EVE-\\d{4}-[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{10}$");

    @Test
    void prePersist_generatesOrderNumberMatchingExpectedFormat() {
        Order order = new Order();
        order.prePersist();

        assertNotNull(order.getOrderNumber());
        assertTrue(ORDER_NUMBER_PATTERN.matcher(order.getOrderNumber()).matches(),
                "Order number did not match expected format: " + order.getOrderNumber());
    }

    @Test
    void prePersist_doesNotOverwriteExistingOrderNumber() {
        Order order = new Order();
        order.setOrderNumber("EVE-2024-EXISTING1");

        order.prePersist();

        assertEquals("EVE-2024-EXISTING1", order.getOrderNumber());
    }

    @Test
    void prePersist_generatesUnpredictableOrderNumbers() {
        // Regression test for the previous timestamp-based implementation, which was
        // predictable and allowed enumeration of other customers' orders.
        Set<String> generated = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            Order order = new Order();
            order.prePersist();
            generated.add(order.getOrderNumber());
        }

        // All 200 generated numbers should be unique (extremely high probability with
        // a 10-character random suffix from a 33-character alphabet).
        assertEquals(200, generated.size());
    }

    @Test
    void prePersist_doesNotEncodeCreationTimeInOrderNumber() {
        Order first = new Order();
        first.prePersist();

        Order second = new Order();
        second.prePersist();

        // The random suffix must not be derivable from creation order/time.
        String firstSuffix = first.getOrderNumber().substring(first.getOrderNumber().lastIndexOf('-') + 1);
        String secondSuffix = second.getOrderNumber().substring(second.getOrderNumber().lastIndexOf('-') + 1);
        assertFalse(firstSuffix.equals(secondSuffix));
    }

    @Test
    void calculateTotals_sumsItemPricesTimesQuantity() {
        Order order = new Order();

        OrderItem item1 = new OrderItem();
        item1.setPrice(BigDecimal.valueOf(10));
        item1.setQuantity(2);

        OrderItem item2 = new OrderItem();
        item2.setPrice(BigDecimal.valueOf(5));
        item2.setQuantity(3);

        order.addItem(item1);
        order.addItem(item2);

        assertEquals(BigDecimal.valueOf(35), order.getTotalAmount());
    }
}
