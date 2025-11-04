package com.everage.eshop.controller;

import com.everage.eshop.dto.AddToCartRequest;
import com.everage.eshop.dto.CartDto;
import com.everage.eshop.dto.UpdateCartItemRequest;
import com.everage.eshop.service.CartService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/{cartUuid}/items")
    public CartDto addToCart(@PathVariable UUID cartUuid, @RequestBody AddToCartRequest request) {
        return cartService.addToCart(cartUuid, request);
    }

    @GetMapping("/{cartUuid}")
    public CartDto getCart(@PathVariable UUID cartUuid) {
        return cartService.getCart(cartUuid);
    }

    @DeleteMapping("/{cartUuid}/items/{itemUuid}")
    public void removeFromCart(@PathVariable UUID cartUuid, @PathVariable UUID itemUuid) {
        cartService.removeFromCart(cartUuid, itemUuid);
    }

    @PutMapping("/{cartUuid}/items/{itemUuid}")
    public CartDto updateCartItemQuantity(@PathVariable UUID cartUuid, @PathVariable UUID itemUuid, @RequestBody UpdateCartItemRequest request) {
        return cartService.updateCartItemQuantity(cartUuid, itemUuid, request.quantity());
    }
}