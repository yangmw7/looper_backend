package com.example.game_backend.service;

import com.example.game_backend.controller.dto.EquipItemRequest;
import com.example.game_backend.controller.dto.PlayerEquipmentResponse;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.PlayerStatsRepository;
import com.example.game_backend.repository.ItemRepository;
import com.example.game_backend.repository.entity.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerEquipmentServiceImpl implements PlayerEquipmentService {

    private final PlayerStatsRepository playerStatsRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final ObjectMapper objectMapper;

    // 슬롯 순서 정의 (equiped 배열의 인덱스와 매핑)
    // DB: ["helmet_id", "armor_id", "pants_id", "mainWeapon_id", "subWeapon_id"]
    private static final Map<Integer, String> SLOT_INDEX_MAP = Map.of(
            0, "helmet",
            1, "armor",
            2, "pants",
            3, "mainWeapon",
            4, "subWeapon"
    );

    // 아이템 ID에서 타입 추론 (첫 두자리)
    private static final Map<String, String> ITEM_TYPE_MAP = Map.of(
            "01", "mainWeapon",
            "02", "subWeapon",
            "03", "helmet",
            "04", "armor",
            "05", "pants"
    );

    @Override
    @Transactional(readOnly = true)
    public PlayerEquipmentResponse getPlayerEquipment(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        PlayerStats stats = playerStatsRepository.findByMember(member)
                .orElseThrow(() -> new IllegalArgumentException("플레이어 스탯을 찾을 수 없습니다"));

        log.info("DB에서 가져온 inventory JSON: {}", stats.getInventory());
        log.info("DB에서 가져온 equipped JSON: {}", stats.getEquiped());

        // 장착 아이템 파싱 (배열 → Map)
        Map<String, PlayerEquipmentResponse.ItemInfo> equipped = parseEquippedArray(stats.getEquiped());

        // 인벤토리 파싱 (이중 직렬화된 배열 → List)
        List<PlayerEquipmentResponse.ItemInfo> inventory = parseInventoryArray(stats.getInventory());

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

        // 현재 장착 정보를 배열로 파싱
        List<String> equippedArray = parseEquippedToArray(stats.getEquiped());

        // 현재 인벤토리 파싱
        List<InventorySlot> inventorySlots = parseInventoryToSlots(stats.getInventory());

        // 슬롯 인덱스 찾기
        Integer slotIndex = getSlotIndex(request.getSlot());
        if (slotIndex == null) {
            throw new IllegalArgumentException("유효하지 않은 슬롯입니다: " + request.getSlot());
        }

        // 인벤토리에서 해당 아이템 찾기
        InventorySlot targetSlot = inventorySlots.stream()
                .filter(slot -> request.getItemId().equals(slot.getItemid()) && slot.getEa() > 0)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("인벤토리에 해당 아이템이 없습니다"));

        // 기존에 장착된 아이템이 있다면 인벤토리로 되돌리기
        String oldItemId = equippedArray.get(slotIndex);
        if (oldItemId != null && !oldItemId.trim().isEmpty() && !oldItemId.equals("00000")) {
            // 기존 장착 아이템을 인벤토리에 추가
            addItemToInventory(inventorySlots, oldItemId);
        }

        // 새 아이템 장착
        equippedArray.set(slotIndex, request.getItemId());

        // 인벤토리에서 해당 아이템 제거 (ea 감소)
        removeItemFromInventory(inventorySlots, request.getItemId());

        // DB 저장
        try {
            stats.setEquiped(objectMapper.writeValueAsString(equippedArray));
            stats.setInventory(serializeInventorySlots(inventorySlots));
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

        // 현재 장착 정보를 배열로 파싱
        List<String> equippedArray = parseEquippedToArray(stats.getEquiped());

        // 현재 인벤토리 파싱
        List<InventorySlot> inventorySlots = parseInventoryToSlots(stats.getInventory());

        // 슬롯 인덱스 찾기
        Integer slotIndex = getSlotIndex(slot);
        if (slotIndex == null) {
            throw new IllegalArgumentException("유효하지 않은 슬롯입니다: " + slot);
        }

        // 장착된 아이템 ID 가져오기
        String itemId = equippedArray.get(slotIndex);
        if (itemId == null || itemId.trim().isEmpty() || itemId.equals("00000")) {
            throw new IllegalArgumentException("해당 슬롯에 장착된 아이템이 없습니다");
        }

        // 인벤토리에 아이템 추가
        addItemToInventory(inventorySlots, itemId);

        // 해당 슬롯 비우기
        equippedArray.set(slotIndex, "");

        // DB 저장
        try {
            stats.setEquiped(objectMapper.writeValueAsString(equippedArray));
            stats.setInventory(serializeInventorySlots(inventorySlots));
            playerStatsRepository.save(stats);

            log.info("아이템 해제 완료 - 사용자: {}, 슬롯: {}, 아이템: {}", username, slot, itemId);
        } catch (Exception e) {
            log.error("장비 정보 저장 실패", e);
            throw new RuntimeException("장비 정보 저장에 실패했습니다");
        }

        return getPlayerEquipment(username);
    }

    /**
     * 장비 보너스 계산
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

        // 소수점 2자리까지 반올림
        hp = Math.round(hp * 100.0f) / 100.0f;
        atk = Math.round(atk * 100.0f) / 100.0f;
        def = Math.round(def * 100.0f) / 100.0f;
        cri = Math.round(cri * 100.0f) / 100.0f;
        crid = Math.round(crid * 100.0f) / 100.0f;
        spd = Math.round(spd * 100.0f) / 100.0f;
        jmp = Math.round(jmp * 100.0f) / 100.0f;
        ats = Math.round(ats * 100.0f) / 100.0f;

        return new PlayerEquipmentResponse.EquipmentBonus(
                hp, atk, def, cri, crid, spd, jmp, ats, jcnt
        );
    }

    /**
     * 장착 장비 배열 파싱
     * DB 형식: ["03001","00000","05001","01006","02001"]
     * → Map<String, ItemInfo>
     */
    private Map<String, PlayerEquipmentResponse.ItemInfo> parseEquippedArray(String equippedJson) {
        log.info("parseEquippedArray 호출 - 입력값: {}", equippedJson);

        if (equippedJson == null || equippedJson.trim().isEmpty() || equippedJson.equals("[]")) {
            log.warn("equipped가 비어있음");
            return new HashMap<>();
        }

        try {
            // JSON 배열 파싱
            List<String> itemIds = objectMapper.readValue(equippedJson, new TypeReference<List<String>>() {});

            // 유효한 아이템 ID만 추출 (빈 문자열, "00000" 제외)
            List<String> validItemIds = itemIds.stream()
                    .filter(id -> id != null && !id.trim().isEmpty() && !id.equals("00000"))
                    .distinct()
                    .collect(Collectors.toList());

            if (validItemIds.isEmpty()) {
                return new HashMap<>();
            }

            // DB에서 아이템 정보 조회
            List<Item> items = itemRepository.findAllByIdWithDetails(validItemIds);
            Map<String, Item> itemMap = items.stream()
                    .collect(Collectors.toMap(Item::getId, item -> item));

            // 결과 Map 생성
            Map<String, PlayerEquipmentResponse.ItemInfo> result = new HashMap<>();

            for (int i = 0; i < itemIds.size(); i++) {
                String itemId = itemIds.get(i);
                if (itemId == null || itemId.trim().isEmpty() || itemId.equals("00000")) {
                    continue;
                }

                String slotName = SLOT_INDEX_MAP.get(i);
                if (slotName == null) {
                    continue;
                }

                Item item = itemMap.get(itemId);
                if (item != null) {
                    result.put(slotName, convertToItemInfo(item));
                }
            }

            log.info("parseEquippedArray 성공 - 결과 크기: {}", result.size());
            return result;

        } catch (Exception e) {
            log.error("장착 장비 JSON 파싱 실패: {}", equippedJson, e);
            return new HashMap<>();
        }
    }

    /**
     * 인벤토리 배열 파싱
     * DB 형식: ["{\\"index\\":0,\\"itemid\\":\\"04001\\",\\"ea\\":1}", ...]
     * → List<ItemInfo>
     */
    private List<PlayerEquipmentResponse.ItemInfo> parseInventoryArray(String inventoryJson) {
        log.info("parseInventoryArray 호출 - 입력값: {}", inventoryJson);

        if (inventoryJson == null || inventoryJson.trim().isEmpty() || inventoryJson.equals("[]")) {
            log.warn("inventory가 비어있음");
            return new ArrayList<>();
        }

        try {
            // 1단계: 외부 JSON 배열 파싱
            List<String> inventoryStrings = objectMapper.readValue(inventoryJson, new TypeReference<List<String>>() {});

            // 2단계: 각 문자열을 다시 JSON 객체로 파싱
            List<InventorySlot> slots = new ArrayList<>();
            for (String slotJson : inventoryStrings) {
                try {
                    InventorySlot slot = objectMapper.readValue(slotJson, InventorySlot.class);
                    // ea > 0이고 itemid가 유효한 경우만 추가
                    if (slot.getEa() != null && slot.getEa() > 0 &&
                            slot.getItemid() != null && !slot.getItemid().trim().isEmpty() &&
                            !slot.getItemid().equals("00000")) {
                        slots.add(slot);
                    }
                } catch (Exception e) {
                    log.warn("인벤토리 슬롯 파싱 실패: {}", slotJson, e);
                }
            }

            if (slots.isEmpty()) {
                return new ArrayList<>();
            }

            // 3단계: 유효한 아이템 ID 추출
            List<String> itemIds = slots.stream()
                    .map(InventorySlot::getItemid)
                    .distinct()
                    .collect(Collectors.toList());

            // 4단계: DB에서 아이템 정보 조회
            List<Item> items = itemRepository.findAllByIdWithDetails(itemIds);
            Map<String, Item> itemMap = items.stream()
                    .collect(Collectors.toMap(Item::getId, item -> item));

            // 5단계: ItemInfo로 변환
            List<PlayerEquipmentResponse.ItemInfo> result = slots.stream()
                    .map(slot -> {
                        Item item = itemMap.get(slot.getItemid());
                        return item != null ? convertToItemInfo(item) : null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.info("parseInventoryArray 성공 - 결과 크기: {}", result.size());
            return result;

        } catch (Exception e) {
            log.error("인벤토리 JSON 파싱 실패: {}", inventoryJson, e);
            return new ArrayList<>();
        }
    }

    /**
     * Item 엔티티를 ItemInfo DTO로 변환
     */
    private PlayerEquipmentResponse.ItemInfo convertToItemInfo(Item item) {
        // 한국어 이름 찾기 (한글이 포함된 것 우선)
        String name = item.getNames().stream()
                .filter(n -> "ko".equals(n.getLang()))
                .filter(n -> n.getValue() != null && n.getValue().matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) // 한글 포함
                .findFirst()
                .map(ItemName::getValue)
                .orElseGet(() ->
                        item.getNames().stream()
                                .filter(n -> "ko".equals(n.getLang()))
                                .findFirst()
                                .map(ItemName::getValue)
                                .orElse(item.getId())
                );

        // 한국어 설명 찾기 (한글이 포함된 것 우선)
        String desc = item.getDescriptions().stream()
                .filter(d -> "ko".equals(d.getLang()))
                .filter(d -> d.getValue() != null && d.getValue().matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) // 한글 포함
                .findFirst()
                .map(ItemDescription::getValue)
                .orElseGet(() ->
                        item.getDescriptions().stream()
                                .filter(d -> "ko".equals(d.getLang()))
                                .findFirst()
                                .map(ItemDescription::getValue)
                                .orElse("")
                );

        // 아이템 타입 추론
        String type = getItemType(item.getId());

        // 스탯 정보 파싱
        Map<String, Float> stats = new HashMap<>();
        for (ItemAttribute attr : item.getAttributes()) {
            if ("ADD".equals(attr.getOp())) {
                stats.put(attr.getStat().toLowerCase(), attr.getValue());
            }
        }

        return PlayerEquipmentResponse.ItemInfo.builder()
                .id(item.getId())
                .type(type)
                .name(name)
                .desc(desc)
                .icon("🗡️") // 기본 아이콘 (필요시 타입별로 다르게)
                .imageUrl(item.getImageUrl())
                .hp(stats.containsKey("hp") ? stats.get("hp") : null)
                .atk(stats.containsKey("atk") ? stats.get("atk") : null)
                .def(stats.containsKey("def") ? stats.get("def") : null)
                .cri(stats.containsKey("cri") ? stats.get("cri") : null)
                .crid(stats.containsKey("crid") ? stats.get("crid") : null)
                .spd(stats.containsKey("spd") ? stats.get("spd") : null)
                .jmp(stats.containsKey("jmp") ? stats.get("jmp") : null)
                .ats(stats.containsKey("ats") ? stats.get("ats") : null)
                .jcnt(stats.containsKey("jcnt") ? stats.get("jcnt").intValue() : null)
                .build();
    }

    /**
     * 아이템 ID로 타입 추론
     */
    private String getItemType(String itemId) {
        if (itemId == null || itemId.length() < 2) {
            return "unknown";
        }
        String prefix = itemId.substring(0, 2);
        return ITEM_TYPE_MAP.getOrDefault(prefix, "unknown");
    }

    /**
     * 슬롯 이름으로 인덱스 찾기
     */
    private Integer getSlotIndex(String slotName) {
        return SLOT_INDEX_MAP.entrySet().stream()
                .filter(entry -> entry.getValue().equals(slotName))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * equipped JSON을 배열로 파싱 (장착/해제 시 사용)
     */
    private List<String> parseEquippedToArray(String equippedJson) {
        if (equippedJson == null || equippedJson.trim().isEmpty() || equippedJson.equals("[]")) {
            // 기본값: 5개 슬롯 모두 빈 문자열
            return new ArrayList<>(Arrays.asList("", "", "", "", ""));
        }

        try {
            List<String> result = objectMapper.readValue(equippedJson, new TypeReference<List<String>>() {});
            // 5개 슬롯 보장
            while (result.size() < 5) {
                result.add("");
            }
            return result;
        } catch (Exception e) {
            log.error("equipped 배열 파싱 실패: {}", equippedJson, e);
            return new ArrayList<>(Arrays.asList("", "", "", "", ""));
        }
    }

    /**
     * 인벤토리 슬롯 DTO (내부 파싱용)
     */
    @lombok.Data
    private static class InventorySlot {
        private Integer index;
        private String itemid;
        private Integer ea;
    }

    /**
     * 인벤토리 JSON을 InventorySlot 리스트로 파싱
     */
    private List<InventorySlot> parseInventoryToSlots(String inventoryJson) {
        if (inventoryJson == null || inventoryJson.trim().isEmpty() || inventoryJson.equals("[]")) {
            // 기본 15칸 인벤토리 생성
            List<InventorySlot> slots = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                InventorySlot slot = new InventorySlot();
                slot.setIndex(i);
                slot.setItemid("");
                slot.setEa(0);
                slots.add(slot);
            }
            return slots;
        }

        try {
            List<String> inventoryStrings = objectMapper.readValue(inventoryJson, new TypeReference<List<String>>() {});
            List<InventorySlot> slots = new ArrayList<>();

            for (String slotJson : inventoryStrings) {
                try {
                    InventorySlot slot = objectMapper.readValue(slotJson, InventorySlot.class);
                    slots.add(slot);
                } catch (Exception e) {
                    log.warn("인벤토리 슬롯 파싱 실패: {}", slotJson, e);
                }
            }

            return slots;
        } catch (Exception e) {
            log.error("인벤토리 파싱 실패: {}", inventoryJson, e);
            return new ArrayList<>();
        }
    }

    /**
     * 인벤토리에 아이템 추가 (빈 슬롯이나 같은 아이템이 있는 슬롯에)
     */
    private void addItemToInventory(List<InventorySlot> inventorySlots, String itemId) {
        // 1. 같은 아이템이 있는 슬롯 찾기
        for (InventorySlot slot : inventorySlots) {
            if (itemId.equals(slot.getItemid()) && slot.getEa() < 99) {
                slot.setEa(slot.getEa() + 1);
                log.info("기존 슬롯에 아이템 추가: index={}, itemId={}, ea={}", slot.getIndex(), itemId, slot.getEa());
                return;
            }
        }

        // 2. 빈 슬롯 찾기
        for (InventorySlot slot : inventorySlots) {
            if (slot.getItemid() == null || slot.getItemid().trim().isEmpty() || slot.getEa() == 0) {
                slot.setItemid(itemId);
                slot.setEa(1);
                log.info("빈 슬롯에 아이템 추가: index={}, itemId={}", slot.getIndex(), itemId);
                return;
            }
        }

        throw new RuntimeException("인벤토리가 가득 찼습니다");
    }

    /**
     * 인벤토리에서 아이템 제거
     */
    private void removeItemFromInventory(List<InventorySlot> inventorySlots, String itemId) {
        for (InventorySlot slot : inventorySlots) {
            if (itemId.equals(slot.getItemid()) && slot.getEa() > 0) {
                slot.setEa(slot.getEa() - 1);
                if (slot.getEa() == 0) {
                    slot.setItemid("");
                }
                log.info("인벤토리에서 아이템 제거: index={}, itemId={}, ea={}", slot.getIndex(), itemId, slot.getEa());
                return;
            }
        }
        throw new IllegalArgumentException("인벤토리에 해당 아이템이 없습니다: " + itemId);
    }

    /**
     * InventorySlot 리스트를 JSON 문자열로 직렬화
     */
    private String serializeInventorySlots(List<InventorySlot> slots) {
        try {
            List<String> slotJsons = new ArrayList<>();
            for (InventorySlot slot : slots) {
                String slotJson = objectMapper.writeValueAsString(slot);
                slotJsons.add(slotJson);
            }
            return objectMapper.writeValueAsString(slotJsons);
        } catch (Exception e) {
            log.error("인벤토리 직렬화 실패", e);
            throw new RuntimeException("인벤토리 저장에 실패했습니다");
        }
    }
}