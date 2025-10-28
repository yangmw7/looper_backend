package com.example.game_backend.api;

import com.example.game_backend.controller.dto.ItemRequest;
import com.example.game_backend.controller.dto.ItemResponse;
import com.example.game_backend.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final ObjectMapper objectMapper;

    // ========== 모든 사용자 접근 가능 (게임 가이드용) ==========
    @GetMapping
    public List<ItemResponse> getAllItems() {
        return itemService.getAllItems();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItem(@PathVariable String id) {
        ItemResponse item = itemService.getItem(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    // ========== Admin용 상세 조회 (전체 데이터) ==========
    @GetMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItemResponse> getItemForAdmin(@PathVariable String id) {
        ItemResponse item = itemService.getItemFull(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    // ========== 관리자만 접근 가능 (관리 기능) ==========
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createItem(
            @RequestPart("item") String itemJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            ItemRequest request = objectMapper.readValue(itemJson, ItemRequest.class);
            log.info("아이템 생성 요청: ID={}, 이미지={}", request.getId(),
                    imageFile != null ? imageFile.getOriginalFilename() : "없음");

            ItemResponse created = itemService.createItem(request, imageFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            log.error("아이템 생성 실패", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "아이템 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateItem(
            @PathVariable String id,
            @RequestPart("item") String itemJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            ItemRequest request = objectMapper.readValue(itemJson, ItemRequest.class);
            log.info("아이템 수정 요청: ID={}, 이미지={}", id,
                    imageFile != null ? imageFile.getOriginalFilename() : "없음");

            ItemResponse updated = itemService.updateItem(id, request, imageFile);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("아이템 수정 실패", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "아이템 수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteItem(@PathVariable String id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}