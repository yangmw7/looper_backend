package com.example.game_backend.service;

import com.example.game_backend.controller.dto.ItemRequest;
import com.example.game_backend.controller.dto.ItemResponse;
import com.example.game_backend.repository.ItemRepository;
import com.example.game_backend.repository.entity.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    private ItemResponse toResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getRarity(),
                item.getDescription(),
                item.getAttributes(),
                item.getSkills()
        );
    }

    @Override
    public ItemResponse createItem(ItemRequest request) {
        Item item = new Item();
        item.setId(request.getId());
        item.setName(request.getName());
        item.setRarity(request.getRarity());
        item.setDescription(request.getDescription());
        item.setAttributes(request.getAttributes());
        item.setSkills(request.getSkills());
        return toResponse(itemRepository.save(item));
    }

    @Override
    public ItemResponse getItem(String id) {
        return itemRepository.findById(id).map(this::toResponse).orElse(null);
    }

    @Override
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ItemResponse updateItem(String id, ItemRequest request) {
        return itemRepository.findById(id).map(item -> {
            item.setName(request.getName());
            item.setRarity(request.getRarity());
            item.setDescription(request.getDescription());
            item.setAttributes(request.getAttributes());
            item.setSkills(request.getSkills());
            return toResponse(itemRepository.save(item));
        }).orElse(null);
    }

    @Override
    public void deleteItem(String id) {
        itemRepository.deleteById(id);
    }
}
