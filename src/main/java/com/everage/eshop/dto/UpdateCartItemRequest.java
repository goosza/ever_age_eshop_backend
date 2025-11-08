package com.everage.eshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to update cart item quantity")
public record UpdateCartItemRequest(
        @Schema(description = "New quantity for the item",
                example = "5",
                required = true,
                minimum = "1")
        Integer quantity
) {}