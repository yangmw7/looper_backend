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

    private static final Map<Integer, String> SLOT_INDEX_MAP = Map.of(
            0, "helmet",
            1, "armor",
            2, "pants",
            3, "mainWeapon",
            4, "subWeapon"
    );

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

        Map<String, PlayerEquipmentResponse.ItemInfo> equipped = parseEquippedArray(stats.getEquiped());
        List<PlayerEquipmentResponse.ItemInfo> inventory = parseInventoryArray(stats.getInventory());
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

        List<String> equippedArray = parseEquippedToArray(stats.getEquiped());
        List<InventorySlot> inventorySlots = parseInventoryToSlots(stats.getInventory());
        Integer slotIndex = getSlotIndex(request.getSlot());
        if (slotIndex == null) throw new IllegalArgumentException("유효하지 않은 슬롯입니다: " + request.getSlot());

        // 기존 장비 해제
        String oldItemId = equippedArray.get(slotIndex);
        if (oldItemId != null && !oldItemId.trim().isEmpty() && !oldItemId.equals("00000")) {
            removeItemStatsFromPlayer(stats, oldItemId);
            addItemToInventory(inventorySlots, oldItemId);
        }

        // 새 장비 장착
        addItemStatsToPlayer(stats, request.getItemId());
        equippedArray.set(slotIndex, request.getItemId());
        removeItemFromInventory(inventorySlots, request.getItemId());

        try {
            stats.setEquiped(objectMapper.writeValueAsString(equippedArray));
            stats.setInventory(serializeInventorySlots(inventorySlots));
            playerStatsRepository.saveAndFlush(stats);
        } catch (Exception e) {
            log.error("장비 저장 실패", e);
            throw new RuntimeException("장비 저장 실패");
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

        List<String> equippedArray = parseEquippedToArray(stats.getEquiped());
        List<InventorySlot> inventorySlots = parseInventoryToSlots(stats.getInventory());
        Integer slotIndex = getSlotIndex(slot);
        if (slotIndex == null) throw new IllegalArgumentException("유효하지 않은 슬롯: " + slot);

        String itemId = equippedArray.get(slotIndex);
        if (itemId == null || itemId.trim().isEmpty() || itemId.equals("00000"))
            throw new IllegalArgumentException("장착된 아이템이 없습니다");

        removeItemStatsFromPlayer(stats, itemId);
        addItemToInventory(inventorySlots, itemId);
        equippedArray.set(slotIndex, "");

        try {
            stats.setEquiped(objectMapper.writeValueAsString(equippedArray));
            stats.setInventory(serializeInventorySlots(inventorySlots));
            playerStatsRepository.saveAndFlush(stats);
        } catch (Exception e) {
            throw new RuntimeException("해제 저장 실패", e);
        }

        return getPlayerEquipment(username);
    }

    private void addItemStatsToPlayer(PlayerStats player, String itemId) {
        List<Item> items = itemRepository.findAllByIdWithDetails(List.of(itemId));
        if (items.isEmpty()) throw new IllegalArgumentException("아이템을 찾을 수 없습니다: " + itemId);
        Item item = items.get(0);
        for (ItemAttribute attr : item.getAttributes()) {
            if (!"ADD".equals(attr.getOp())) continue;
            float v = attr.getValue();
            switch (attr.getStat().toLowerCase()) {
                case "hp": player.setHp(player.getHp() + v); break;
                case "atk": player.setAtk(player.getAtk() + v); break;
                case "def": player.setDef(player.getDef() + v); break;
                case "cri": player.setCri(player.getCri() + v); break;
                case "crid": player.setCrid(player.getCrid() + v); break;
                case "spd": player.setSpd(player.getSpd() + v); break;
                case "jmp": player.setJmp(player.getJmp() + v); break;
                case "ats": player.setAts(player.getAts() + v); break;
                case "jcnt": player.setJcnt(player.getJcnt() + (int)v); break;
            }
        }
    }

    private void removeItemStatsFromPlayer(PlayerStats player, String itemId) {
        List<Item> items = itemRepository.findAllByIdWithDetails(List.of(itemId));
        if (items.isEmpty()) throw new IllegalArgumentException("아이템을 찾을 수 없습니다: " + itemId);
        Item item = items.get(0);
        for (ItemAttribute attr : item.getAttributes()) {
            if (!"ADD".equals(attr.getOp())) continue;
            float v = attr.getValue();
            switch (attr.getStat().toLowerCase()) {
                case "hp": player.setHp(player.getHp() - v); break;
                case "atk": player.setAtk(player.getAtk() - v); break;
                case "def": player.setDef(player.getDef() - v); break;
                case "cri": player.setCri(player.getCri() - v); break;
                case "crid": player.setCrid(player.getCrid() - v); break;
                case "spd": player.setSpd(player.getSpd() - v); break;
                case "jmp": player.setJmp(player.getJmp() - v); break;
                case "ats": player.setAts(player.getAts() - v); break;
                case "jcnt": player.setJcnt(player.getJcnt() - (int)v); break;
            }
        }
    }

    private PlayerEquipmentResponse.EquipmentBonus calculateEquipmentBonus(
            Map<String, PlayerEquipmentResponse.ItemInfo> equipped) {

        if (equipped == null || equipped.isEmpty())
            return new PlayerEquipmentResponse.EquipmentBonus(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0);

        float hp=0,atk=0,def=0,cri=0,crid=0,spd=0,jmp=0,ats=0; int jcnt=0;
        for (PlayerEquipmentResponse.ItemInfo item : equipped.values()) {
            if (item == null) continue;
            hp += nz(item.getHp()); atk += nz(item.getAtk()); def += nz(item.getDef());
            cri += nz(item.getCri()); crid += nz(item.getCrid()); spd += nz(item.getSpd());
            jmp += nz(item.getJmp()); ats += nz(item.getAts());
            jcnt += item.getJcnt() == null ? 0 : item.getJcnt();
        }
        return new PlayerEquipmentResponse.EquipmentBonus(hp, atk, def, cri, crid, spd, jmp, ats, jcnt);
    }

    private float nz(Float v){ return v==null?0f:v; }

    private Map<String, PlayerEquipmentResponse.ItemInfo> parseEquippedArray(String equippedJson) {
        if (equippedJson == null || equippedJson.isBlank() || equippedJson.equals("[]")) return new HashMap<>();
        try {
            List<String> itemIds = objectMapper.readValue(equippedJson,new TypeReference<List<String>>(){});
            List<String> valid = itemIds.stream()
                    .filter(i->i!=null&&!i.isBlank()&&!i.equals("00000")).collect(Collectors.toList());
            if (valid.isEmpty()) return new HashMap<>();
            List<Item> items = itemRepository.findAllByIdWithDetails(valid);
            Map<String, Item> map = items.stream().collect(Collectors.toMap(Item::getId,i->i));
            Map<String, PlayerEquipmentResponse.ItemInfo> result = new HashMap<>();
            for (int i=0;i<itemIds.size();i++){
                String id=itemIds.get(i);
                if (id==null||id.isBlank()||id.equals("00000")) continue;
                String slot=SLOT_INDEX_MAP.get(i);
                if (slot!=null && map.containsKey(id))
                    result.put(slot, convertToItemInfo(map.get(id)));
            }
            return result;
        }catch(Exception e){ return new HashMap<>(); }
    }

    private List<PlayerEquipmentResponse.ItemInfo> parseInventoryArray(String inventoryJson){
        if (inventoryJson==null||inventoryJson.isBlank()||inventoryJson.equals("[]")) return new ArrayList<>();
        try{
            List<String> arr=objectMapper.readValue(inventoryJson,new TypeReference<List<String>>(){});
            List<InventorySlot> slots=new ArrayList<>();
            for(String s:arr){
                try{ InventorySlot slot=objectMapper.readValue(s,InventorySlot.class);
                    if(slot.getEa()!=null&&slot.getEa()>0&&slot.getItemid()!=null&&!slot.getItemid().isBlank())
                        slots.add(slot);
                }catch(Exception ignored){}
            }
            if(slots.isEmpty()) return new ArrayList<>();
            List<String> ids=slots.stream().map(InventorySlot::getItemid).distinct().collect(Collectors.toList());
            List<Item> items=itemRepository.findAllByIdWithDetails(ids);
            Map<String,Item> map=items.stream().collect(Collectors.toMap(Item::getId,i->i));
            return slots.stream().map(s->map.containsKey(s.getItemid())?convertToItemInfo(map.get(s.getItemid())):null)
                    .filter(Objects::nonNull).collect(Collectors.toList());
        }catch(Exception e){ return new ArrayList<>(); }
    }

    private PlayerEquipmentResponse.ItemInfo convertToItemInfo(Item item){
        String name=item.getNames().stream()
                .filter(n->"ko".equals(n.getLang()))
                .findFirst().map(ItemName::getValue).orElse(item.getId());
        String desc=item.getDescriptions().stream()
                .filter(d->"ko".equals(d.getLang()))
                .findFirst().map(ItemDescription::getValue).orElse("");
        String type=getItemType(item.getId());
        Map<String,Float> st=new HashMap<>();
        for(ItemAttribute a:item.getAttributes()) if("ADD".equals(a.getOp())) st.put(a.getStat().toLowerCase(),a.getValue());
        return PlayerEquipmentResponse.ItemInfo.builder()
                .id(item.getId()).type(type).name(name).desc(desc)
                .imageUrl(item.getImageUrl())
                .hp(st.get("hp")).atk(st.get("atk")).def(st.get("def")).cri(st.get("cri"))
                .crid(st.get("crid")).spd(st.get("spd")).jmp(st.get("jmp")).ats(st.get("ats"))
                .jcnt(st.get("jcnt")==null?null:st.get("jcnt").intValue()).build();
    }

    private String getItemType(String itemId){
        if(itemId==null||itemId.length()<2)return"unknown";
        return ITEM_TYPE_MAP.getOrDefault(itemId.substring(0,2),"unknown");
    }

    private Integer getSlotIndex(String slotName){
        return SLOT_INDEX_MAP.entrySet().stream()
                .filter(e->e.getValue().equals(slotName))
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }

    private List<String> parseEquippedToArray(String equippedJson){
        try{
            List<String> list=objectMapper.readValue(equippedJson,new TypeReference<List<String>>(){});
            while(list.size()<5) list.add("");
            return list;
        }catch(Exception e){ return new ArrayList<>(Arrays.asList("","","","","")); }
    }

    @lombok.Data
    private static class InventorySlot{
        private Integer index;
        private String itemid;
        private Integer ea;
    }

    private List<InventorySlot> parseInventoryToSlots(String inventoryJson){
        try{
            List<String> arr=objectMapper.readValue(inventoryJson,new TypeReference<List<String>>(){});
            List<InventorySlot> list=new ArrayList<>();
            for(String s:arr){ try{ list.add(objectMapper.readValue(s,InventorySlot.class)); }catch(Exception ignored){} }
            return list;
        }catch(Exception e){
            List<InventorySlot> def=new ArrayList<>();
            for(int i=0;i<15;i++){ InventorySlot s=new InventorySlot(); s.setIndex(i); s.setItemid(""); s.setEa(0); def.add(s);}
            return def;
        }
    }

    private void addItemToInventory(List<InventorySlot> slots,String itemId){
        for(InventorySlot s:slots){
            if(itemId.equals(s.getItemid())&&s.getEa()<99){ s.setEa(s.getEa()+1); return; }
        }
        for(InventorySlot s:slots){
            if(s.getItemid()==null||s.getItemid().isBlank()||s.getEa()==0){
                s.setItemid(itemId); s.setEa(1); return;
            }
        }
        throw new RuntimeException("인벤토리가 가득 찼습니다");
    }

    private void removeItemFromInventory(List<InventorySlot> slots,String itemId){
        for(InventorySlot s:slots){
            if(itemId.equals(s.getItemid())&&s.getEa()>0){
                s.setEa(s.getEa()-1);
                if(s.getEa()==0)s.setItemid("");
                return;
            }
        }
        throw new IllegalArgumentException("인벤토리에 해당 아이템이 없습니다: "+itemId);
    }

    private String serializeInventorySlots(List<InventorySlot> slots){
        try{
            List<String> jsons=new ArrayList<>();
            for(InventorySlot s:slots) jsons.add(objectMapper.writeValueAsString(s));
            return objectMapper.writeValueAsString(jsons);
        }catch(Exception e){ throw new RuntimeException("인벤토리 직렬화 실패",e); }
    }
}
