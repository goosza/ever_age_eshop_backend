package com.everage.eshop.controller;

import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.dto.CollectionMultipartRequest;
import com.everage.eshop.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Collections", description = "Collection management APIs")
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping(path = "/all", produces = "application/json")
    @Operation(
            summary = "Get all collections",
            description = "Returns a list of all collections available in the shop"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of collections",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CollectionDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public List<CollectionDto> getAllCollections() {
        return collectionService.getAllCollections();
    }

    @GetMapping(path = "/{uuid}", produces = "application/json")
    @Operation(
            summary = "Get collection by UUID",
            description = "Returns information about a specific collection by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Collection found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CollectionDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Collection not found with the provided UUID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public CollectionDto getCollectionById(
            @Parameter(
                    description = "Collection UUID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID uuid) {
        return collectionService.getCollectionByUuid(uuid);
    }

    @PostMapping(path = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create new collection",
            description = "Creates a new collection. Send collection fields as form parts alongside optional image files."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Collection created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CollectionDto.class))),
            @ApiResponse(responseCode = "409", description = "Collection with this name already exists", content = @Content)
    })
    public CollectionDto createCollection(
            @RequestPart("collection") CollectionMultipartRequest collectionRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return collectionService.createCollection(collectionRequest, images);
    }

    @PutMapping(path = "/{uuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Update collection",
            description = "Updates a collection. Pass existingImageUrls in the collection part to keep specific images; " +
                    "omitted URLs will be deleted from R2. New files in 'images' will be uploaded and appended."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Collection updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CollectionDto.class))),
            @ApiResponse(responseCode = "404", description = "Collection not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Collection with this name already exists", content = @Content)
    })
    public CollectionDto updateCollection(
            @Parameter(description = "Collection UUID", required = true) @PathVariable UUID uuid,
            @RequestPart("collection") CollectionMultipartRequest collectionRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return collectionService.updateCollection(uuid, collectionRequest, images);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete collection",
            description = "Deletes a collection by UUID. Items in this collection will have their collection reference removed."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Collection deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Collection not found with the provided UUID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public void deleteCollection(
            @Parameter(
                    description = "Collection UUID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID uuid) {
        collectionService.deleteCollection(uuid);
    }

    @PostMapping(path = "/{collectionUuid}/items/{itemUuid}", produces = "application/json")
    @Operation(
            summary = "Add item to collection",
            description = "Adds an existing item to a specific collection"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Item added to collection successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CollectionDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Collection or item not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Item already belongs to this collection",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public CollectionDto addItemToCollection(
            @Parameter(
                    description = "Collection UUID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID collectionUuid,
            @Parameter(
                    description = "Item UUID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174001"
            )
            @PathVariable UUID itemUuid) {
        return collectionService.addItemToCollection(collectionUuid, itemUuid);
    }

    @DeleteMapping(path = "/{collectionUuid}/items/{itemUuid}/remove", produces = "application/json")
    @Operation(
            summary = "Remove item from collection",
            description = "Removes an item from a specific collection. The item itself is not deleted."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Item removed from collection successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CollectionDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Item does not belong to this collection",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Collection or item not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public CollectionDto removeItemFromCollection(
            @Parameter(
                    description = "Collection UUID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID collectionUuid,
            @Parameter(
                    description = "Item UUID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174001"
            )
            @PathVariable UUID itemUuid) {
        return collectionService.removeItemFromCollection(collectionUuid, itemUuid);
    }
}