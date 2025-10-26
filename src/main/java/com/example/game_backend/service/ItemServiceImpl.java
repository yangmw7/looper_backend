package com.example.game_backend.service;

import com.example.game_backend.controller.dto.ItemRequest;
import com.example.game_backend.controller.dto.ItemResponse;
import com.example.game_backend.repository.ItemRepository;
import com.example.game_backend.repository.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CloudinaryService cloudinaryService;

    // 한글만 필터링하는 헬퍼 메서드
    private List<String> filterKoreanOnly(List<ItemName> names) {
        return names.stream()
                .filter(name -> containsKorean(name.getValue()))
                .map(ItemName::getValue)
                .toList();
    }

    private List<String> filterKoreanDescriptions(List<ItemDescription> descriptions) {
        return descriptions.stream()
                .filter(desc -> containsKorean(desc.getValue()))
                .map(ItemDescription::getValue)
                .toList();
    }

    // 한글 포함 여부 확인
    private boolean containsKorean(String text) {
        if (text == null) return false;
        return text.matches(".*[가-힣].*");
    }

    // 엔티티 → Response DTO 변환 (⭐ 한글만 필터링)
    private ItemResponse toResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getRarity(),
                item.isTwoHander(),
                item.isStackable(),
                filterKoreanOnly(item.getNames()), // ⭐ 한글만 필터링
                filterKoreanDescriptions(item.getDescriptions()), // ⭐ 한글만 필터링
                item.getSkills().stream().map(ItemSkill::getSkillId).toList(),
                item.getAttributes().stream()
                        .map(a -> new ItemResponse.AttributeDto(a.getStat(), a.getOp(), a.getValue()))
                        .toList(),
                item.getImageUrl()
        );
    }

    // Request DTO → 엔티티 변환
    private Item toEntity(ItemRequest request) {
        Item item = new Item();
        item.setId(request.getId());
        item.setRarity(request.getRarity());
        item.setTwoHander(request.isTwoHander());
        item.setStackable(request.isStackable());
        item.setImageUrl(request.getImageUrl());

        // names
        List<ItemName> names = new ArrayList<>();
        for (String value : request.getName()) {
            ItemName n = new ItemName();
            n.setItem(item);
            n.setLang("ko");
            n.setValue(value);
            names.add(n);
        }
        item.setNames(names);

        // descriptions
        List<ItemDescription> descs = new ArrayList<>();
        for (String value : request.getDescription()) {
            ItemDescription d = new ItemDescription();
            d.setItem(item);
            d.setLang("ko");
            d.setValue(value);
            descs.add(d);
        }
        item.setDescriptions(descs);

        // skills
        List<ItemSkill> skills = new ArrayList<>();
        for (String skillId : request.getSkills()) {
            ItemSkill s = new ItemSkill();
            s.setItem(item);
            s.setSkillId(skillId);
            skills.add(s);
        }
        item.setSkills(skills);

        // attributes
        List<ItemAttribute> attrs = new ArrayList<>();
        for (ItemRequest.AttributeDto dto : request.getAttributes()) {
            ItemAttribute a = new ItemAttribute();
            a.setItem(item);
            a.setStat(dto.getStat());
            a.setOp(dto.getOp());
            a.setValue(dto.getValue());
            attrs.add(a);
        }
        item.setAttributes(attrs);

        return item;
    }

    @Override
    @Transactional
    public ItemResponse createItem(ItemRequest request, MultipartFile imageFile) {
        try {
            if (itemRepository.existsById(request.getId())) {
                throw new IllegalArgumentException("이미 존재하는 아이템 ID입니다: " + request.getId());
            }

            // Cloudinary에 이미지 업로드
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(imageFile, request.getId());
                request.setImageUrl(imageUrl);
                log.info("아이템 {} 이미지 업로드 완료: {}", request.getId(), imageUrl);
            }

            Item saved = itemRepository.save(toEntity(request));
            return toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("이미 존재하는 아이템 ID입니다: " + request.getId());
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
    @Transactional
    public ItemResponse updateItem(String id, ItemRequest request, MultipartFile imageFile) {
        return itemRepository.findById(id).map(item -> {
            // 기존 이미지 URL 저장 (삭제용)
            String oldImageUrl = item.getImageUrl();

            item.setRarity(request.getRarity());
            item.setTwoHander(request.isTwoHander());
            item.setStackable(request.isStackable());

            // 새 이미지 업로드
            if (imageFile != null && !imageFile.isEmpty()) {
                // 기존 이미지 삭제
                if (oldImageUrl != null) {
                    cloudinaryService.deleteImage(oldImageUrl);
                }

                // 새 이미지 업로드
                String newImageUrl = cloudinaryService.uploadImage(imageFile, id);
                item.setImageUrl(newImageUrl);
                log.info("아이템 {} 이미지 업데이트 완료: {}", id, newImageUrl);
            }

            // 기존 연관 데이터 clear 후 새로 세팅
            item.getNames().clear();
            for (String value : request.getName()) {
                ItemName n = new ItemName();
                n.setItem(item);
                n.setLang("ko");
                n.setValue(value);
                item.getNames().add(n);
            }

            item.getDescriptions().clear();
            for (String value : request.getDescription()) {
                ItemDescription d = new ItemDescription();
                d.setItem(item);
                d.setLang("ko");
                d.setValue(value);
                item.getDescriptions().add(d);
            }

            item.getSkills().clear();
            for (String skillId : request.getSkills()) {
                ItemSkill s = new ItemSkill();
                s.setItem(item);
                s.setSkillId(skillId);
                item.getSkills().add(s);
            }

            item.getAttributes().clear();
            for (ItemRequest.AttributeDto dto : request.getAttributes()) {
                ItemAttribute a = new ItemAttribute();
                a.setItem(item);
                a.setStat(dto.getStat());
                a.setOp(dto.getOp());
                a.setValue(dto.getValue());
                item.getAttributes().add(a);
            }

            return toResponse(itemRepository.save(item));
        }).orElse(null);
    }

    @Override
    @Transactional
    public void deleteItem(String id) {
        itemRepository.findById(id).ifPresent(item -> {
            // Cloudinary 이미지 삭제
            if (item.getImageUrl() != null) {
                cloudinaryService.deleteImage(item.getImageUrl());
                log.info("아이템 {} 이미지 삭제 완료", id);
            }
            itemRepository.deleteById(id);
        });
    }
}