package com.everage.eshop.dto;

import com.everage.eshop.entity.ItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Multipart request for creating or updating an item")
public record ItemRequest(

        @Schema(description = "Item name", example = "ALIEN BAG", required = true)
        String name,

        @Schema(description = "Item description")
        String description,

        @Schema(description = "Item price", example = "1299.99", required = true)
        BigDecimal price,

        @Schema(description = "Item weight in kilograms", example = "0.500", required = true)
        BigDecimal weight,

        @Schema(description = "Item status", example = "ACTIVE", required = true)
        ItemStatus status,

        @Schema(description = "Available quantity", example = "15")
        Integer quantity,

        @Schema(description = "Item color", example = "Black")
        String color,

        @Schema(description = "Existing image URLs to keep (for update). Omit or send empty to remove all existing images.")
        List<String> existingImageUrls
) {}
