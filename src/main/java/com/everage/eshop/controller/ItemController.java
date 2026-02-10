package com.everage.eshop.controller;

import com.everage.eshop.dto.ItemDto;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

//import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/items")
@Tag(name = "Items", description = "API for managing shop items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping(path = "/all", produces = "application/json")
    @Operation(
            summary = "Get all items",
            description = "Returns a complete list of all items available in the shop"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of items",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public List<ItemDto> getAllItems() {
        return itemService.getAllItems();
    }

    @GetMapping(path = "/{uuid}", produces = "application/json")
    @Operation(
            summary = "Get item by UUID",
            description = "Returns information about a specific item by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Item found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Item not found with the specified UUID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public ItemDto getItemById(
            @Parameter(
                    description = "Item UUID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID uuid) {
        return itemService.getItemById(uuid);
    }

    @PostMapping(path = "/add", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create new item",
            description = "Creates a new item in the shop. Item status must match quantity: " +
                    "OUT_OF_STOCK if quantity is 0, and ACTIVE/INACTIVE if quantity > 0"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Item successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid item data: " +
                            "- Item with this name already exists, " +
                            "- Invalid status for quantity (e.g., OUT_OF_STOCK with positive quantity), " +
                            "- Status must be OUT_OF_STOCK when quantity is 0",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Item with this name already exists",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public ItemDto addItem(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New item data. Note: status must be OUT_OF_STOCK if quantity is 0 or null, " +
                            "and cannot be OUT_OF_STOCK if quantity > 0",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ItemDto.class)
                    )
            )
            @RequestBody ItemDto itemDto) {
        return itemService.createItem(itemDto);
    }

    @PutMapping(path = "/{uuid}/update", consumes = "application/json", produces = "application/json")
    @Operation(
            summary = "Update existing item",
            description = "Updates information about an existing item. Item status must match quantity: " +
                    "OUT_OF_STOCK if quantity is 0, and ACTIVE/INACTIVE if quantity > 0"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Item successfully updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data: " +
                            "- Invalid status for quantity (e.g., OUT_OF_STOCK with positive quantity), " +
                            "- Status must be OUT_OF_STOCK when quantity is 0",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Item not found with the specified UUID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Name already taken by another item",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public ItemDto updateItem(
            @Parameter(
                    description = "UUID of the item to update",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID uuid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated item data. Note: status must be OUT_OF_STOCK if quantity is 0 or null, " +
                            "and cannot be OUT_OF_STOCK if quantity > 0",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ItemDto.class)
                    )
            )
            @RequestBody ItemDto itemDto) {
        return itemService.updateItem(uuid, itemDto);
    }

    @DeleteMapping(path = "/{uuid}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete item",
            description = "Deletes an item from the shop. If the item belongs to a collection, " +
                    "it will be removed from the collection as well."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Item successfully deleted",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Item not found with the specified UUID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public void deleteItem(
            @Parameter(
                    description = "UUID of the item to delete",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID uuid) {
        itemService.deleteItem(uuid);
    }
}