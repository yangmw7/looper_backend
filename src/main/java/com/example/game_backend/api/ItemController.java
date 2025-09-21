package com.example.game_backend.api;

import com.example.game_backend.controller.dto.ItemRequest;
import com.example.game_backend.controller.dto.ItemResponse;
import com.example.game_backend.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemResponse createItem(@RequestBody ItemRequest request) {
        return itemService.createItem(request);
    }

    @GetMapping("/{id}")
    public ItemResponse getItem(@PathVariable String id) {
        return itemService.getItem(id);
    }

    @GetMapping
    public List<ItemResponse> getAllItems() {
        return itemService.getAllItems();
    }

    @PutMapping("/{id}")
    public ItemResponse updateItem(@PathVariable String id, @RequestBody ItemRequest request) {
        return itemService.updateItem(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable String id) {
        itemService.deleteItem(id);
    }
}
