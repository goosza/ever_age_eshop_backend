package com.everage.eshop.dto;

import com.everage.eshop.entity.Cart;
import com.everage.eshop.entity.CartItem;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CartMapper {
    private final ItemMapper itemMapper;

    public CartDto toDto(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream()
                .map(this::toCartItemDto)
                .toList();
        return new CartDto(cart.getUuid(), items);
    }

    private CartItemDto toCartItemDto(CartItem cartItem) {
        return new CartItemDto(
                cartItem.getUuid(),
                itemMapper.toDto(cartItem.getItem()),
                cartItem.getQuantity()
        );
    }
}