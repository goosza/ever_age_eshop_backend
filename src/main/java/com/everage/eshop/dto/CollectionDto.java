package com.everage.eshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Collection DTO representing a product collection")
public record CollectionDto(

        @Schema(description = "Unique identifier (UUID)",
                example = "550e8400-e29b-41d4-a716-446655440000",
                accessMode = Schema.AccessMode.READ_ONLY)
        UUID uuid,

        @NotBlank(message = "Collection name is required")
        @Size(max = 100, message = "Name must be less than 100 characters")
        @Schema(description = "Collection name",
                example = "Alien",
                required = true,
                maxLength = 100)
        String name,

        @Size(max = 5000, message = "Description must be less than 5000 characters")
        @Schema(description = "Collection description",
                example = "Futuristic alien-inspired designs",
                maxLength = 5000)
        String description,

        @Schema(description = "List of image URLs",
                example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
        List<String> imageUrls,

        @Schema(description = "Number of items in this collection",
                example = "15",
                accessMode = Schema.AccessMode.READ_ONLY)
        Integer itemCount,

        @Schema(description = "Creation timestamp",
                example = "2024-02-07T18:30:00",
                accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp",
                example = "2024-02-08T10:15:00",
                accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime updatedAt
) {
    // Compact constructor для валидации или значений по умолчанию
    public CollectionDto {
        if (imageUrls == null) {
            imageUrls = List.of();
        }
    }
}
