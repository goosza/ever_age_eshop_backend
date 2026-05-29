package com.everage.eshop.controller;

import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.dto.CollectionMultipartRequest;
import com.everage.eshop.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/admin/collections")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Collections", description = "Collection management for administrators")
public class AdminCollectionController {

    private final CollectionService collectionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new collection")
    @RequestBody(content = @Content(encoding = @Encoding(name = "collection", contentType = MediaType.APPLICATION_JSON_VALUE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Collection created"),
            @ApiResponse(responseCode = "409", description = "Collection with this name already exists", content = @Content)
    })
    public CollectionDto createCollection(
            @RequestPart("collection") CollectionMultipartRequest collectionRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return collectionService.createCollection(collectionRequest, images);
    }

    @PutMapping(path = "/{uuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update collection")
    @RequestBody(content = @Content(encoding = @Encoding(name = "collection", contentType = MediaType.APPLICATION_JSON_VALUE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Collection updated"),
            @ApiResponse(responseCode = "404", description = "Collection not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Name already taken", content = @Content)
    })
    public CollectionDto updateCollection(
            @Parameter(description = "Collection UUID", required = true) @PathVariable UUID uuid,
            @RequestPart("collection") CollectionMultipartRequest collectionRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return collectionService.updateCollection(uuid, collectionRequest, images);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete collection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Collection deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Collection not found", content = @Content)
    })
    public void deleteCollection(@PathVariable UUID uuid) {
        collectionService.deleteCollection(uuid);
    }

    @PostMapping(path = "/{collectionUuid}/items/{itemUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add item to collection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item added to collection"),
            @ApiResponse(responseCode = "404", description = "Collection or item not found", content = @Content)
    })
    public CollectionDto addItemToCollection(
            @PathVariable UUID collectionUuid,
            @PathVariable UUID itemUuid) {
        return collectionService.addItemToCollection(collectionUuid, itemUuid);
    }

    @DeleteMapping(path = "/{collectionUuid}/items/{itemUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Remove item from collection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item removed from collection"),
            @ApiResponse(responseCode = "404", description = "Collection or item not found", content = @Content)
    })
    public CollectionDto removeItemFromCollection(
            @PathVariable UUID collectionUuid,
            @PathVariable UUID itemUuid) {
        return collectionService.removeItemFromCollection(collectionUuid, itemUuid);
    }
}
