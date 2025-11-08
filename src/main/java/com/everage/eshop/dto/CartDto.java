package com.everage.eshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Shopping cart data model")
public record CartDto(
        @Schema(description = "Unique cart identifier",
                example = "123e4567-e89b-12d3-a456-426614174000",
                accessMode = Schema.AccessMode.READ_ONLY)
        UUID uuid,

        @Schema(description = "List of items in the cart")
        List<CartItemDto> items
) {}