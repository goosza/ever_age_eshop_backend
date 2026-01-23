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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/items")
@Tag(name = "Items", description = "API for managing shop items")
public class ItemController {
    private final ItemService itemService;

    /**
     * API endpoint to retrieve all items.
     * @return list of {@link ItemDto}
     */
    @GetMapping(path = "/all", produces = "application/json")
    @Operation(
            summary = "Get all items",
            description = "Returns a complete list of all items available in the shop"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of items",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ItemDto.class)
            )
    )
    public List<ItemDto> getAllItems() {
        return itemService.getAllItems();
    }

    /**
     * API endpoint to retrieve an item by its ID.
     * @param id
     * @return {@link ItemDto}
     */
    @GetMapping(path = "/{id}", produces = "application/json")
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
            )
    })
    public ItemDto getItemById(
            @Parameter(
                    description = "Item UUID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id) {
        return itemService.getItemById(id);
    }

    /**
     * API endpoint to add a new item.
     * @param {@link ItemDto}
     * @return {@link ItemDto}
     */
    @PostMapping(path = "/add", consumes = "application/json", produces = "application/json")
    @Operation(
            summary = "Create new item",
            description = "Creates a new item in the shop. Item status must match quantity: " +
                    "OUT_OF_STOCK if quantity is 0, and ACTIVE/INACTIVE if quantity > 0"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
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

    /**
     * API endpoint to update an existing item.
     * @param {@link ItemDto}
     * @return {@link ItemDto}
     */
    @PutMapping(path = "/{id}/update", consumes = "application/json", produces = "application/json")
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
                    responseCode = "404",
                    description = "Item not found with the specified UUID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data: " +
                            "- Name already taken by another item, " +
                            "- Invalid status for quantity (e.g., OUT_OF_STOCK with positive quantity), " +
                            "- Status must be OUT_OF_STOCK when quantity is 0",
                    content = @Content
            )
    })
    public ItemDto updateItem(
            @Parameter(
                    description = "UUID of the item to update",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated item data. Note: status must be OUT_OF_STOCK if quantity is 0 or null, " +
                            "and cannot be OUT_OF_STOCK if quantity > 0",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ItemDto.class)
                    )
            )
            @RequestBody ItemDto itemDto) {
        return itemService.updateItem(id, itemDto);
    }
}
