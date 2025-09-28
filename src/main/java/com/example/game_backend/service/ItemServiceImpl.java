package com.example.game_backend.service;

import com.example.game_backend.controller.dto.ItemRequest;
import com.example.game_backend.controller.dto.ItemResponse;
import com.example.game_backend.repository.ItemRepository;
import com.example.game_backend.repository.entity.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        System.out.println("=== CREATE ITEM DEBUG ===");
        System.out.println("Request ID: " + request.getId());
        System.out.println("Request Name: " + request.getName());
        System.out.println("Request Rarity: " + request.getRarity());

        try {
            // existsById 체크
            boolean exists = itemRepository.existsById(request.getId());
            System.out.println("Item exists check: " + exists);

            if (exists) {
                System.out.println("Throwing IllegalArgumentException for duplicate ID: " + request.getId());
                throw new IllegalArgumentException("이미 존재하는 아이템 ID입니다: " + request.getId());
            }

            Item item = new Item();
            item.setId(request.getId());
            item.setName(request.getName());
            item.setRarity(request.getRarity());
            item.setDescription(request.getDescription());
            item.setAttributes(request.getAttributes());
            item.setSkills(request.getSkills());

            System.out.println("Attempting to save item...");
            Item saved = itemRepository.save(item);
            System.out.println("Item saved successfully with ID: " + saved.getId());

            return toResponse(saved);

        } catch (DataIntegrityViolationException e) {
            System.out.println("DataIntegrityViolationException caught: " + e.getMessage());
            throw new IllegalArgumentException("이미 존재하는 아이템 ID입니다: " + request.getId());
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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