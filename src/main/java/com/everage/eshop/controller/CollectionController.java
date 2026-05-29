package com.everage.eshop.controller;

import com.everage.eshop.dto.CollectionDto;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Collections", description = "Public collection catalog API")
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping(path = "/all", produces = "application/json")
    @Operation(summary = "Get all collections")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of collections",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CollectionDto.class)))
    })
    public List<CollectionDto> getAllCollections() {
        return collectionService.getAllCollections();
    }

    @GetMapping(path = "/{uuid}", produces = "application/json")
    @Operation(summary = "Get collection by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Collection found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CollectionDto.class))),
            @ApiResponse(responseCode = "404", description = "Collection not found", content = @Content)
    })
    public CollectionDto getCollectionById(
            @Parameter(description = "Collection UUID", required = true) @PathVariable UUID uuid) {
        return collectionService.getCollectionByUuid(uuid);
    }
}
