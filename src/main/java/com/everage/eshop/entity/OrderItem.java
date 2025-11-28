package com.everage.eshop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ============================================
    // Relationships
    // ============================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;  // Your existing entity Item

    // ============================================
    // Snapshot item data at the order creation moment
    // (to save history even if item changed)
    // ============================================
    @Column(name = "item_name", nullable = false)
    private String itemName;
    @Column(name = "item_description", columnDefinition = "TEXT")
    private String itemDescription;
    @Column(name = "item_image_url", length = 500)
    private String itemImageUrl;  // Main pic (first from the list)

    // ============================================
    // Order Details
    // ============================================
    @Column(nullable = false)
    private Integer quantity;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;  // Price for one
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;  // quantity * price
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ============================================
    // Lifecycle Callbacks
    // ============================================
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();

        // Auto-calculate subtotal
        if (subtotal == null && price != null && quantity != null) {
            subtotal = price.multiply(BigDecimal.valueOf(quantity));
        }

        // Copy item data snapshot
        if (item != null) {
            if (itemName == null) {
                itemName = item.getName();
            }
            if (itemDescription == null) {
                itemDescription = item.getDescription();
            }
            if (itemImageUrl == null && item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
                itemImageUrl = item.getImageUrls().get(0);  // First image
            }
            if (price == null) {
                price = item.getPrice();
                subtotal = price.multiply(BigDecimal.valueOf(quantity));
            }
        }
    }
}