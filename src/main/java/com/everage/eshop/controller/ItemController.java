package com.everage.eshop.controller;

import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/items")
@Tag(name = "Items", description = "Public item catalog API")
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

    @GetMapping(path = "/collection/{collectionUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get items by collection UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Items retrieved"),
            @ApiResponse(responseCode = "404", description = "Collection not found", content = @Content)
    })
    public List<ItemDto> getItemsByCollectionUuid(
            @Parameter(description = "Collection UUID", required = true) @PathVariable UUID collectionUuid) {
        return itemService.getItemsByCollectionUuid(collectionUuid);
    }
}
