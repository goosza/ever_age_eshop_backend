package com.everage.eshop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "order")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;
    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    private String orderNumber;

    // ============================================
    // Customer Information
    // ============================================
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(nullable = false)
    private String email;
    @Column(length = 20)
    private String phone;
    @Column(name = "birth_date")
    private LocalDate birthDate;

    // ============================================
    // Delivery Address
    // ============================================
    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;
    @Column(nullable = false, length = 100)
    private String city;
    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;
    @Column(nullable = false, length = 100)
    private String country;

    // ============================================
    // Order Details
    // ============================================
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    @Column(name = "items_count", nullable = false)
    private Integer itemsCount = 0;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status = OrderStatus.PENDING;

    // ============================================
    // Shipping Information
    // ============================================
    @Column(name = "shipping_provider", length = 50)
    private String shippingProvider = "DPD";
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;
    @Column(name = "tracking_url", columnDefinition = "TEXT")
    private String trackingUrl;

    // ============================================
    // Notes
    // ============================================
    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    // ============================================
    // Relationships
    // ============================================
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    // ============================================
    // Timestamps
    // ============================================
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    // ============================================
    // Lifecycle Callbacks
    // ============================================
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

    // ============================================
    // Helper Methods
    // ============================================
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

    public void addStatusHistory(OrderStatusHistory history) {
        statusHistory.add(history);
        history.setOrder(this);
    }

    public void calculateTotals() {
        this.itemsCount = items.size();
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void generateTrackingUrl() {
        if (trackingNumber != null && !trackingNumber.isEmpty()) {
            this.trackingUrl = "https://www.dpd.ru/ols/trace?cargo=" + trackingNumber;
        }
    }

    private String generateOrderNumber() {
        // Format: EVE-2024-001234
        return "EVE-" + LocalDateTime.now().getYear() + "-" +
                String.format("%06d", System.currentTimeMillis() % 1000000);
    }
}