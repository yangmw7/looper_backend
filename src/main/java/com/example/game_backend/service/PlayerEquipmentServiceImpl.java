package com.example.game_backend.service;

import com.example.game_backend.controller.dto.EquipItemRequest;
import com.example.game_backend.controller.dto.PlayerEquipmentResponse;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.PlayerStatsRepository;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.PlayerStats;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerEquipmentServiceImpl implements PlayerEquipmentService {

    private final PlayerStatsRepository playerStatsRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public PlayerEquipmentResponse getPlayerEquipment(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        PlayerStats stats = playerStatsRepository.findByMember(member)
                .orElseThrow(() -> new IllegalArgumentException("플레이어 스탯을 찾을 수 없습니다"));

        // 디버깅 로그 추가
        log.info("DB에서 가져온 inventory JSON: {}", stats.getInventory());
        log.info("DB에서 가져온 equipped JSON: {}", stats.getEquiped());

        Map<String, PlayerEquipmentResponse.ItemInfo> equipped = parseEquipped(stats.getEquiped());
        List<PlayerEquipmentResponse.ItemInfo> inventory = parseInventory(stats.getInventory());

        // 파싱 후 로그 추가
        log.info("파싱된 inventory 크기: {}", inventory.size());
        log.info("파싱된 equipped 크기: {}", equipped.size());
        log.info("파싱된 inventory 내용: {}", inventory);

        PlayerEquipmentResponse.EquipmentBonus bonus = calculateEquipmentBonus(equipped);

        return PlayerEquipmentResponse.builder()
                .stats(PlayerEquipmentResponse.StatsDto.from(stats, bonus))
                .equipped(equipped != null ? equipped : new HashMap<>())
                .inventory(inventory != null ? inventory : new ArrayList<>())
                .build();
    }

    @Override
    @Transactional
    public PlayerEquipmentResponse equipItem(String username, EquipItemRequest request) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        PlayerStats stats = playerStatsRepository.findByMember(member)
                .orElseThrow(() -> new IllegalArgumentException("플레이어 스탯을 찾을 수 없습니다"));

        Map<String, PlayerEquipmentResponse.ItemInfo> equipped = parseEquipped(stats.getEquiped());
        List<PlayerEquipmentResponse.ItemInfo> inventory = parseInventory(stats.getInventory());

        PlayerEquipmentResponse.ItemInfo itemToEquip = inventory.stream()
                .filter(item -> item.getId().equals(request.getItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("인벤토리에 해당 아이템이 없습니다"));

        if (!itemToEquip.getType().equals(request.getSlot())) {
            throw new IllegalArgumentException("해당 슬롯에 장착할 수 없는 아이템입니다");
        }

        equipped.put(request.getSlot(), itemToEquip);

        try {
            stats.setEquiped(objectMapper.writeValueAsString(equipped));
            playerStatsRepository.save(stats);

            log.info("아이템 장착 완료 - 사용자: {}, 아이템: {}, 슬롯: {}",
                    username, request.getItemId(), request.getSlot());
        } catch (Exception e) {
            log.error("장비 정보 저장 실패", e);
            throw new RuntimeException("장비 정보 저장에 실패했습니다");
        }

        return getPlayerEquipment(username);
    }

    @Override
    @Transactional
    public PlayerEquipmentResponse unequipItem(String username, String slot) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        PlayerStats stats = playerStatsRepository.findByMember(member)
                .orElseThrow(() -> new IllegalArgumentException("플레이어 스탯을 찾을 수 없습니다"));

        Map<String, PlayerEquipmentResponse.ItemInfo> equipped = parseEquipped(stats.getEquiped());

        if (!equipped.containsKey(slot) || equipped.get(slot) == null) {
            throw new IllegalArgumentException("해당 슬롯에 장착된 아이템이 없습니다");
        }

        equipped.remove(slot);

        try {
            stats.setEquiped(objectMapper.writeValueAsString(equipped));
            playerStatsRepository.save(stats);

            log.info("아이템 해제 완료 - 사용자: {}, 슬롯: {}", username, slot);
        } catch (Exception e) {
            log.error("장비 정보 저장 실패", e);
            throw new RuntimeException("장비 정보 저장에 실패했습니다");
        }

        return getPlayerEquipment(username);
    }

    /**
     * 장비 보너스 계산 - null 체크 강화
     */
    private PlayerEquipmentResponse.EquipmentBonus calculateEquipmentBonus(
            Map<String, PlayerEquipmentResponse.ItemInfo> equipped) {

        if (equipped == null || equipped.isEmpty()) {
            return new PlayerEquipmentResponse.EquipmentBonus(
                    0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0
            );
        }

        float hp = 0.0f;
        float atk = 0.0f;
        float def = 0.0f;
        float cri = 0.0f;
        float crid = 0.0f;
        float spd = 0.0f;
        float jmp = 0.0f;
        float ats = 0.0f;
        int jcnt = 0;

        for (PlayerEquipmentResponse.ItemInfo item : equipped.values()) {
            if (item != null) {
                hp += (item.getHp() != null ? item.getHp() : 0.0f);
                atk += (item.getAtk() != null ? item.getAtk() : 0.0f);
                def += (item.getDef() != null ? item.getDef() : 0.0f);
                cri += (item.getCri() != null ? item.getCri() : 0.0f);
                crid += (item.getCrid() != null ? item.getCrid() : 0.0f);
                spd += (item.getSpd() != null ? item.getSpd() : 0.0f);
                jmp += (item.getJmp() != null ? item.getJmp() : 0.0f);
                ats += (item.getAts() != null ? item.getAts() : 0.0f);
                jcnt += (item.getJcnt() != null ? item.getJcnt() : 0);
            }
        }

        return new PlayerEquipmentResponse.EquipmentBonus(
                hp, atk, def, cri, crid, spd, jmp, ats, jcnt
        );
    }

    /**
     * 장착 장비 JSON 파싱 - 빈 문자열, "{}" 등 처리
     */
    private Map<String, PlayerEquipmentResponse.ItemInfo> parseEquipped(String equippedJson) {
        log.info("parseEquipped 호출 - 입력값: {}", equippedJson);

        if (equippedJson == null || equippedJson.trim().isEmpty() || equippedJson.equals("{}")) {
            log.warn("equipped가 비어있음");
            return new HashMap<>();
        }

        try {
            Map<String, PlayerEquipmentResponse.ItemInfo> result = objectMapper.readValue(equippedJson,
                    new TypeReference<Map<String, PlayerEquipmentResponse.ItemInfo>>() {});
            log.info("parseEquipped 성공 - 결과 크기: {}", result != null ? result.size() : 0);
            return result != null ? result : new HashMap<>();
        } catch (Exception e) {
            log.error("장착 장비 JSON 파싱 실패: {}", equippedJson, e);
            return new HashMap<>();
        }
    }

    /**
     * 인벤토리 JSON 파싱 - 빈 문자열, "[]" 등 처리
     */
    private List<PlayerEquipmentResponse.ItemInfo> parseInventory(String inventoryJson) {
        log.info("parseInventory 호출 - 입력값: {}", inventoryJson);

        if (inventoryJson == null || inventoryJson.trim().isEmpty() || inventoryJson.equals("[]")) {
            log.warn("inventory가 비어있음");
            return new ArrayList<>();
        }

        try {
            List<PlayerEquipmentResponse.ItemInfo> result = objectMapper.readValue(inventoryJson,
                    new TypeReference<List<PlayerEquipmentResponse.ItemInfo>>() {});
            log.info("parseInventory 성공 - 결과 크기: {}", result != null ? result.size() : 0);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("인벤토리 JSON 파싱 실패: {}", inventoryJson, e);
            return new ArrayList<>();
        }
    }
}