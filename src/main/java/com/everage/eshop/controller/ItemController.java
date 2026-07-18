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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    private static final int MAX_PAGE_SIZE = 100;

    @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all items", description = "Returns the full catalog, unpaginated. Kept for backward compatibility.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved list of items"))
    public List<ItemDto> getAllItems() {
        return itemService.getAllItems();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get items (paginated)",
            description = "Returns a page of items. Defaults to page 0, size 20."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved page of items"))
    public Page<ItemDto> getItemsPaged(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max " + MAX_PAGE_SIZE + ")") @RequestParam(defaultValue = "20") int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by("name").ascending());
        return itemService.getAllItems(pageRequest);
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
