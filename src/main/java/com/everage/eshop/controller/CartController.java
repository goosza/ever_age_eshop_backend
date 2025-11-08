package com.everage.eshop.controller;

import com.everage.eshop.dto.AddToCartRequest;
import com.everage.eshop.dto.CartDto;
import com.everage.eshop.dto.UpdateCartItemRequest;
import com.everage.eshop.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart", description = "API for managing shopping cart")
public class CartController {
    private final CartService cartService;

    @PostMapping("/{cartUuid}/items")
    @Operation(
            summary = "Add item to cart",
            description = "Adds specified quantity of an item to the customer's cart"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Item successfully added to cart",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CartDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cart or item not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content
            )
    })
    public CartDto addToCart(
            @Parameter(
                    description = "Cart UUID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID cartUuid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Data for adding item to cart",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AddToCartRequest.class)
                    )
            )
            @RequestBody AddToCartRequest request) {
        log.info("Adding item to cart: {}", cartUuid);
        return cartService.addToCart(cartUuid, request);
    }

    @GetMapping("/{cartUuid}")
    @Operation(
            summary = "Get cart contents",
            description = "Returns complete information about the cart, including all items"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cart successfully retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CartDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cart not found",
                    content = @Content
            )
    })
    public CartDto getCart(
            @Parameter(
                    description = "Cart UUID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID cartUuid) {
        log.info("Fetching cart: {}", cartUuid);
        return cartService.getCart(cartUuid);
    }

    @DeleteMapping("/{cartUuid}/items/{itemUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove item from cart",
            description = "Completely removes specified item from cart (regardless of quantity)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Item successfully removed from cart"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cart or item not found",
                    content = @Content
            )
    })
    public void removeFromCart(
            @Parameter(
                    description = "Cart UUID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID cartUuid,
            @Parameter(
                    description = "UUID of the item to remove",
                    required = true,
                    example = "987e6543-e21b-12d3-a456-426614174999"
            )
            @PathVariable UUID itemUuid) {
        log.info("Removing item {} from cart {}", itemUuid, cartUuid);
        cartService.removeFromCart(cartUuid, itemUuid);
    }

    @PutMapping("/{cartUuid}/items/{itemUuid}")
    @Operation(
            summary = "Update item quantity in cart",
            description = "Changes the quantity of specified item in cart to a new value"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Quantity successfully updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CartDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cart or item not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid quantity (e.g., negative value)",
                    content = @Content
            )
    })
    public CartDto updateCartItemQuantity(
            @Parameter(
                    description = "Cart UUID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID cartUuid,
            @Parameter(
                    description = "UUID of the item in cart",
                    required = true,
                    example = "987e6543-e21b-12d3-a456-426614174999"
            )
            @PathVariable UUID itemUuid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New item quantity",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateCartItemRequest.class)
                    )
            )
            @RequestBody UpdateCartItemRequest request) {
        log.info("Updating quantity for item {} in cart {} to {}", itemUuid, cartUuid, request.quantity());
        return cartService.updateCartItemQuantity(cartUuid, itemUuid, request.quantity());
    }
}