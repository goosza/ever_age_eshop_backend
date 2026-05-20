package com.everage.eshop.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.everage.eshop.entity.ItemStatus;

@Schema(description = "Item data model")
public record ItemDto(
        @Schema(description = "Unique item identifier",
                example = "123e4567-e89b-12d3-a456-426614174000",
                accessMode = Schema.AccessMode.READ_ONLY)
        UUID uuid,

        @Schema(description = "Item name",
                example = "ALIEN BAG",
                required = true,
                maxLength = 255)
        String name,

        @Schema(description = "Item description",
                example = "Purse made in collaboration with czech brand Praga By Matous Fiala.",
                maxLength = 1000)
        String description,

        @ArraySchema(
                schema = @Schema(
                        description = "Item image URL",
                        implementation = String.class,
                        example = "/products/product-one.jpg"
                ),
                arraySchema = @Schema(
                        description = "List of item image URLs"
                )
        )
        List<String> imageUrls,

        @Schema(description = "Item price",
                example = "1299.99",
                required = true,
                minimum = "0")
        BigDecimal price,

        @Schema(description = "Item weight in kilograms",
                example = "0.500",
                required = true,
                minimum = "0.001")
        BigDecimal weight,

        @Schema(description = "Item status (availability)",
                example = "ACTIVE",
                required = true,
                implementation = ItemStatus.class)
        ItemStatus status,

        @Schema(description = "Available quantity in stock",
                example = "15",
                minimum = "0")
        Integer quantity,

        @Schema(description = "Item color",
                example = "Black",
                maxLength = 100)
        String color,

        @Schema(description = "Collection this item belongs to",
                accessMode = Schema.AccessMode.READ_ONLY)
        CollectionDto collection
) {}