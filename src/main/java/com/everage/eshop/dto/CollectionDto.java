package com.everage.eshop.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Collection data model (with items)")
public record CollectionDto(

        @Schema(description = "Unique collection identifier",
                example = "123e4567-e89b-12d3-a456-426614174000",
                accessMode = Schema.AccessMode.READ_ONLY)
        UUID uuid,

        @Schema(description = "Collection name", example = "Alien")
        String name,

        @Schema(description = "Collection description")
        String description,

        @ArraySchema(schema = @Schema(implementation = String.class))
        List<String> imageUrls,

        @Schema(description = "Items in this collection")
        List<ItemDto> items,

        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime createdAt,

        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime updatedAt
) {}