package com.everage.eshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Multipart request for creating or updating a collection")
public record CollectionMultipartRequest(

        @Schema(description = "Collection name", example = "Alien", required = true)
        String name,

        @Schema(description = "Collection description")
        String description,

        @Schema(description = "Existing image URLs to keep (for update). Omit or send empty to remove all existing images.")
        List<String> existingImageUrls
) {}
