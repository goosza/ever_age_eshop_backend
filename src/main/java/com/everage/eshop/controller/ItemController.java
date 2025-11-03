package com.everage.eshop.controller;

import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.service.ItemService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/items")
public class ItemController {
    private final ItemService itemService;

    /**
     * API endpoint to retrieve all items.
     * @return list of {@link ItemDto}
     */
    @GetMapping(path = "/all", produces = "application/json")
    public List<ItemDto> getAllItems() {
        // Logic to retrieve all items from the database
        return itemService.getAllItems();
    }

    /**
     * API endpoint to retrieve an item by its ID.
     * @param id
     * @return {@link ItemDto}
     */
    @GetMapping(path = "/{id}", produces = "application/json")
    public ItemDto getItemById(@PathVariable UUID id) {
        return itemService.getItemById(id);
    }

    /**
     * API endpoint to add a new item.
     * @param {@link ItemDto}
     * @return {@link ItemDto}
     */
    @PostMapping(path = "/add", consumes = "application/json", produces = "application/json")
    public ItemDto addItem(@RequestBody ItemDto itemDto) {
        return itemService.createItem(itemDto);
    }

    /**
     * API endpoint to update an existing item.
     * @param {@link ItemDto}
     * @return {@link ItemDto}
     */
    @PutMapping(path = "/{id}/update", consumes = "application/json", produces = "application/json")
    public ItemDto updateItem(@PathVariable UUID id,
                              @RequestBody ItemDto itemDto) {
        return itemService.updateItem(id, itemDto);
    }
}
