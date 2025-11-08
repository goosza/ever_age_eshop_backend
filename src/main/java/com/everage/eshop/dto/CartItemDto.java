package com.everage.eshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Cart item data model")
public record CartItemDto(
        @Schema(description = "Unique cart item identifier",
                example = "987e6543-e21b-12d3-a456-426614174999",
                accessMode = Schema.AccessMode.READ_ONLY)
        UUID uuid,

        @Schema(description = "Item details")
        ItemDto item,

        @Schema(description = "Quantity of this item in cart",
                example = "2",
                minimum = "1")
        Integer quantity
) {}