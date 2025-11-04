package com.everage.eshop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "cart_item")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;
    
    @ManyToOne
    @JoinColumn(name = "cart_uuid", nullable = false)
    private Cart cart;
    
    @ManyToOne
    @JoinColumn(name = "item_uuid", nullable = false)
    private Item item;
    
    @Column(nullable = false)
    private Integer quantity;
}