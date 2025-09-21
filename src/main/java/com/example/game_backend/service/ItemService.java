package com.example.game_backend.service;

import com.example.game_backend.controller.dto.ItemRequest;
import com.example.game_backend.controller.dto.ItemResponse;

import java.util.List;

public interface ItemService {
    ItemResponse createItem(ItemRequest request);
    ItemResponse getItem(String id);
    List<ItemResponse> getAllItems();
    ItemResponse updateItem(String id, ItemRequest request);
    void deleteItem(String id);
}
