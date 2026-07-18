package com.everage.eshop.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "orders", uniqueConstraints = {
        @UniqueConstraint(name = "uk_order_number", columnNames = "order_number")
})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID uuid;

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    // Customer Information
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(nullable = false)
    private String email;
    @Column(length = 20)
    private String phone;

    // Order Details
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING;

    // Payment Information
    @Column(name = "stripe_session_id", unique = true, length = 255)
    private String stripeSessionId;

    // Notes
    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;

    // Relationships
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Shipping shipping;

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Payment payment;

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (orderNumber == null) {
            orderNumber = generateOrderNumber();
        }

        calculateTotals();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        calculateTotals();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        calculateTotals();
    }

    public void calculateTotals() {
        this.totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Excludes visually ambiguous characters (0/O, 1/I) to keep order numbers easy to read.
    private static final String ORDER_NUMBER_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int ORDER_NUMBER_RANDOM_LENGTH = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates an order number that is safe to expose on a public tracking endpoint.
     * Uses a cryptographically secure random suffix (not a predictable timestamp) so
     * that order numbers cannot be guessed or enumerated by an attacker.
     */
    private String generateOrderNumber() {
        StringBuilder suffix = new StringBuilder(ORDER_NUMBER_RANDOM_LENGTH);
        for (int i = 0; i < ORDER_NUMBER_RANDOM_LENGTH; i++) {
            int index = SECURE_RANDOM.nextInt(ORDER_NUMBER_ALPHABET.length());
            suffix.append(ORDER_NUMBER_ALPHABET.charAt(index));
        }
        return "EVE-" + LocalDateTime.now().getYear() + "-" + suffix;
    }
}