package com.everage.eshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Request to add an item to cart")
public record AddToCartRequest(
        @Schema(description = "UUID of the item to add",
                example = "987e6543-e21b-12d3-a456-426614174999",
                required = true)
        UUID itemUuid,

        @Schema(description = "Quantity to add",
                example = "2",
                required = true,
                minimum = "1")
        Integer quantity
) {}