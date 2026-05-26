package com.everage.eshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.ForeignKey;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "shippings")
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_uuid",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_shipping_order")
    )
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShippingProvider provider;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(length = 100)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShippingStatus status;

    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;

    // Zasilkovna pickup point fields
    @Column(name = "pickup_point_id", length = 50)
    private String pickupPointId;

    @Column(name = "pickup_point_name", length = 255)
    private String pickupPointName;

    @Column(name = "pickup_point_address", columnDefinition = "TEXT")
    private String pickupPointAddress;

    // Zasilkovna shipment fields
    @Column(name = "shipment_id", length = 100)
    private String shipmentId;

    @Column(name = "label_url", length = 500)
    private String labelUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
