package com.example.game_backend.service;

import com.example.game_backend.controller.dto.ItemRequest;
import com.example.game_backend.controller.dto.ItemResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService {
    ItemResponse createItem(ItemRequest request, MultipartFile imageFile);
    ItemResponse getItem(String id);
    List<ItemResponse> getAllItems();
    ItemResponse updateItem(String id, ItemRequest request, MultipartFile imageFile);
    void deleteItem(String id);
}