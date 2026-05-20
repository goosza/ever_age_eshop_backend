package com.everage.eshop.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ForeignKey;
import org.hibernate.annotations.Type;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;


@Data
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID uuid;
    @Column(nullable = false)
    private String name;
    private String description;
    @Type(JsonType.class)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> imageUrls = new ArrayList<>();
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @Column(nullable = false, precision = 6, scale = 3)
    private BigDecimal weight = BigDecimal.valueOf(0.500); // Weight in kg
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ItemStatus status = ItemStatus.ACTIVE;
    @Column(nullable = false)
    private Integer quantity;
    @Column(length = 100)
    private String color;

    // Relationship with Collection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "collection_uuid",
            foreignKey = @ForeignKey(name = "fk_item_collection")
    )
    private Collection collection;
}
