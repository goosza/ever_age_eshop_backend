package com.everage.eshop.controller;

import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.dto.ItemRequest;
import com.everage.eshop.service.ItemService;
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
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/items")
@Tag(name = "Items", description = "API for managing shop items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all items")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved list of items"))
    public List<ItemDto> getAllItems() {
        return itemService.getAllItems();
    }

    @GetMapping(path = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get item by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item found"),
            @ApiResponse(responseCode = "404", description = "Item not found", content = @Content)
    })
    public ItemDto getItemById(@PathVariable UUID uuid) {
        return itemService.getItemById(uuid);
    }

    @PostMapping(path = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create new item",
            description = "Creates a new item. Send item fields as form parts alongside optional image files."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ItemDto.class))),
            @ApiResponse(responseCode = "409", description = "Item with this name already exists", content = @Content)
    })
    public ItemDto addItem(
            @RequestPart("item") ItemRequest item,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return itemService.createItem(item, images);
    }

    @PutMapping(path = "/{uuid}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Update existing item",
            description = "Updates an item. Pass existingImageUrls in the item part to keep specific images; " +
                    "omitted URLs will be deleted from R2. New files in 'images' will be uploaded and appended."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ItemDto.class))),
            @ApiResponse(responseCode = "404", description = "Item not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Name already taken", content = @Content)
    })
    public ItemDto updateItem(
            @Parameter(description = "Item UUID", required = true) @PathVariable UUID uuid,
            @RequestPart("item") ItemRequest item,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return itemService.updateItem(uuid, item, images);
    }

    @DeleteMapping(path = "/{uuid}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete item", description = "Deletes an item and removes its images from R2.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Item not found", content = @Content)
    })
    public void deleteItem(@PathVariable UUID uuid) {
        itemService.deleteItem(uuid);
    }

    @GetMapping(path = "/collection/{collectionUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get items by collection UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Items retrieved"),
            @ApiResponse(responseCode = "404", description = "Collection not found", content = @Content)
    })
    public List<ItemDto> getItemsByCollectionUuid(@PathVariable UUID collectionUuid) {
        return itemService.getItemsByCollectionUuid(collectionUuid);
    }
}
