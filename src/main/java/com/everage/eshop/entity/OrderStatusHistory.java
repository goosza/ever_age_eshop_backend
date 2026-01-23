package com.everage.eshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 50)
    private OrderStatus oldStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 50)
    private OrderStatus newStatus;
    @Column(name = "changed_by", length = 100)
    private String changedBy = "SYSTEM";
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}