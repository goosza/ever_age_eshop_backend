package com.everage.eshop.controller;

import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.dto.ItemRequest;
import com.everage.eshop.service.ItemService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/items")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Items", description = "Item management for administrators")
public class AdminItemController {

    private final ItemService itemService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new item")
    @RequestBody(content = @Content(encoding = @Encoding(name = "item", contentType = MediaType.APPLICATION_JSON_VALUE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item created"),
            @ApiResponse(responseCode = "409", description = "Item with this name already exists", content = @Content)
    })
    public ItemDto createItem(
            @RequestPart("item") ItemRequest item,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return itemService.createItem(item, images);
    }

    @PutMapping(path = "/{uuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update item")
    @RequestBody(content = @Content(encoding = @Encoding(name = "item", contentType = MediaType.APPLICATION_JSON_VALUE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item updated"),
            @ApiResponse(responseCode = "404", description = "Item not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Name already taken", content = @Content)
    })
    public ItemDto updateItem(
            @Parameter(description = "Item UUID", required = true) @PathVariable UUID uuid,
            @RequestPart("item") ItemRequest item,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return itemService.updateItem(uuid, item, images);
    }

    @PatchMapping(path = "/{uuid}/quantity", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update item quantity",
            description = "quantity=0 sets OUT_OF_STOCK, quantity>0 sets ACTIVE")
    public ItemDto updateQuantity(
            @PathVariable UUID uuid,
            @RequestParam Integer quantity) {
        return itemService.updateQuantity(uuid, quantity);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Item not found", content = @Content)
    })
    public void deleteItem(@PathVariable UUID uuid) {
        itemService.deleteItem(uuid);
    }
}
