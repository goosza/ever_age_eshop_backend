package com.everage.eshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "DTO for creating or updating a collection")
public record CollectionRequest(

        @NotBlank(message = "Collection name is required")
        @Size(max = 100, message = "Name must be less than 100 characters")
        @Schema(description = "Collection name", example = "Alien", required = true)
        String name,

        @Size(max = 5000, message = "Description must be less than 5000 characters")
        @Schema(description = "Collection description", example = "Futuristic alien-inspired designs")
        String description,

        @Schema(description = "List of image URLs")
        List<String> imageUrls
) {
    public CollectionRequest {
        if (imageUrls == null) {
            imageUrls = List.of();
        }
    }
}