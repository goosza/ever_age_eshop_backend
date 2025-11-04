package com.everage.eshop.service;

import com.everage.eshop.dto.AddToCartRequest;
import com.everage.eshop.dto.CartDto;
import com.everage.eshop.dto.CartMapper;
import com.everage.eshop.entity.Cart;
import com.everage.eshop.entity.CartItem;
import com.everage.eshop.entity.Item;
import com.everage.eshop.exception.ItemNotFoundException;
import com.everage.eshop.repository.CartItemRepository;
import com.everage.eshop.repository.CartRepository;
import com.everage.eshop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final CartMapper cartMapper;

    @Transactional
    public CartDto addToCart(UUID cartUuid, AddToCartRequest request) {
        Cart cart = cartRepository.findById(cartUuid)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUuid(cartUuid);
                    cartRepository.persist(newCart);
                    return newCart;
                });

        Item item = itemRepository.findById(request.itemUuid())
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        CartItem existingCartItem = cartItemRepository
                .findByCartUuidAndItemUuid(cartUuid, request.itemUuid())
                .orElse(null);

        if (existingCartItem != null) {
            existingCartItem.setQuantity(existingCartItem.getQuantity() + request.quantity());
            cartItemRepository.persist(existingCartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setItem(item);
            cartItem.setQuantity(request.quantity());
            cartItemRepository.persist(cartItem);
        }

        Cart updatedCart = cartRepository.findById(cartUuid).orElse(cart);
        return cartMapper.toDto(updatedCart);
    }

    public CartDto getCart(UUID cartUuid) {
        Cart cart = cartRepository.findById(cartUuid).orElse(new Cart());
        return cartMapper.toDto(cart);
    }

    @Transactional
    public void removeFromCart(UUID cartUuid, UUID itemUuid) {
        cartItemRepository.findByCartUuidAndItemUuid(cartUuid, itemUuid)
                .ifPresent(cartItemRepository::delete);
    }

    @Transactional
    public CartDto updateCartItemQuantity(UUID cartUuid, UUID itemUuid, Integer quantity) {
        CartItem cartItem = cartItemRepository.findByCartUuidAndItemUuid(cartUuid, itemUuid)
                .orElseThrow(() -> new ItemNotFoundException("Item not found in cart"));
        
        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.persist(cartItem);
        }
        
        Cart updatedCart = cartRepository.findById(cartUuid).orElse(new Cart());
        return cartMapper.toDto(updatedCart);
    }
}