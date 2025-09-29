package com.example.game_backend.service;

import com.example.game_backend.controller.dto.ItemRequest;
import com.example.game_backend.controller.dto.ItemResponse;
import com.example.game_backend.repository.ItemRepository;
import com.example.game_backend.repository.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    // 엔티티 → Response DTO 변환
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
                        .toList()
        );
    }

    // Request DTO → 엔티티 변환
    private Item toEntity(ItemRequest request) {
        Item item = new Item();
        item.setId(request.getId());
        item.setRarity(request.getRarity());
        item.setTwoHander(request.isTwoHander());
        item.setStackable(request.isStackable());

        // names
        List<ItemName> names = new ArrayList<>();
        for (String value : request.getName()) {
            ItemName n = new ItemName();
            n.setItem(item);
            n.setLang("ko"); // TODO: 필요시 언어코드 구분
            n.setValue(value);
            names.add(n);
        }
        item.setNames(names);

        // descriptions
        List<ItemDescription> descs = new ArrayList<>();
        for (String value : request.getDescription()) {
            ItemDescription d = new ItemDescription();
            d.setItem(item);
            d.setLang("ko"); // TODO: 필요시 언어코드 구분
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
    public ItemResponse createItem(ItemRequest request) {
        try {
            if (itemRepository.existsById(request.getId())) {
                throw new IllegalArgumentException("이미 존재하는 아이템 ID입니다: " + request.getId());
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
    public ItemResponse updateItem(String id, ItemRequest request) {
        return itemRepository.findById(id).map(item -> {
            item.setRarity(request.getRarity());
            item.setTwoHander(request.isTwoHander());
            item.setStackable(request.isStackable());

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
    public void deleteItem(String id) {
        itemRepository.deleteById(id);
    }
}
