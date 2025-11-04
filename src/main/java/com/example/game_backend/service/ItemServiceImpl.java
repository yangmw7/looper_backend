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

    // =============================
    // 🟢 Item → Response 변환 (필터 제거)
    // =============================

    // 일반 사용자용 (GameGuide용)
    private ItemResponse toResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getRarity(),
                item.isTwoHander(),
                item.isStackable(),
                item.getNames().stream().map(ItemName::getValue).toList(),
                item.getDescriptions().stream().map(ItemDescription::getValue).toList(),
                item.getSkills().stream().map(ItemSkill::getSkillId).toList(),
                item.getAttributes().stream()
                        .map(a -> new ItemResponse.AttributeDto(a.getStat(), a.getOp(), a.getValue()))
                        .toList(),
                item.getImageUrl()
        );
    }

    // Admin용 (전체 데이터)
    private ItemResponse toResponseFull(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getRarity(),
                item.isTwoHander(),
                item.isStackable(),
                item.getNames().stream().map(ItemName::getValue).toList(),
                item.getDescriptions().stream().map(ItemDescription::getValue).toList(),
                item.getSkills().stream().map(ItemSkill::getSkillId).toList(),
                item.getAttributes().stream()
                        .map(a -> new ItemResponse.AttributeDto(a.getStat(), a.getOp(), a.getValue()))
                        .toList(),
                item.getImageUrl()
        );
    }

    // =============================
    // 🟢 Request DTO → Entity 변환
    // =============================
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
            n.setLang("ko"); // 기본값 ko (원하면 "en"으로 확장 가능)
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

    // =============================
    // 🟢 CRUD 서비스 구현
    // =============================

    @Override
    @Transactional
    public ItemResponse createItem(ItemRequest request, MultipartFile imageFile) {
        try {
            if (itemRepository.existsById(request.getId())) {
                throw new IllegalArgumentException("이미 존재하는 아이템 ID입니다: " + request.getId());
            }

            // Cloudinary 업로드
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(imageFile, request.getId());
                request.setImageUrl(imageUrl);
                log.info("아이템 {} 이미지 업로드 완료: {}", request.getId(), imageUrl);
            }

            Item saved = itemRepository.save(toEntity(request));
            return toResponseFull(saved);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("이미 존재하는 아이템 ID입니다: " + request.getId());
        }
    }

    @Override
    public ItemResponse getItem(String id) {
        return itemRepository.findById(id).map(this::toResponse).orElse(null);
    }

    @Override
    public ItemResponse getItemFull(String id) {
        return itemRepository.findById(id).map(this::toResponseFull).orElse(null);
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
            String oldImageUrl = item.getImageUrl();

            item.setRarity(request.getRarity());
            item.setTwoHander(request.isTwoHander());
            item.setStackable(request.isStackable());

            // 새 이미지 업로드
            if (imageFile != null && !imageFile.isEmpty()) {
                if (oldImageUrl != null) {
                    cloudinaryService.deleteImage(oldImageUrl);
                }
                String newImageUrl = cloudinaryService.uploadImage(imageFile, id);
                item.setImageUrl(newImageUrl);
                log.info("아이템 {} 이미지 업데이트 완료: {}", id, newImageUrl);
            }

            // 이름
            item.getNames().clear();
            for (String value : request.getName()) {
                ItemName n = new ItemName();
                n.setItem(item);
                n.setLang("ko");
                n.setValue(value);
                item.getNames().add(n);
            }

            // 설명
            item.getDescriptions().clear();
            for (String value : request.getDescription()) {
                ItemDescription d = new ItemDescription();
                d.setItem(item);
                d.setLang("ko");
                d.setValue(value);
                item.getDescriptions().add(d);
            }

            // 스킬
            item.getSkills().clear();
            for (String skillId : request.getSkills()) {
                ItemSkill s = new ItemSkill();
                s.setItem(item);
                s.setSkillId(skillId);
                item.getSkills().add(s);
            }

            // 속성
            item.getAttributes().clear();
            for (ItemRequest.AttributeDto dto : request.getAttributes()) {
                ItemAttribute a = new ItemAttribute();
                a.setItem(item);
                a.setStat(dto.getStat());
                a.setOp(dto.getOp());
                a.setValue(dto.getValue());
                item.getAttributes().add(a);
            }

            return toResponseFull(itemRepository.save(item));
        }).orElse(null);
    }

    @Override
    @Transactional
    public void deleteItem(String id) {
        itemRepository.findById(id).ifPresent(item -> {
            if (item.getImageUrl() != null) {
                cloudinaryService.deleteImage(item.getImageUrl());
                log.info("아이템 {} 이미지 삭제 완료", id);
            }
            itemRepository.deleteById(id);
        });
    }
}
