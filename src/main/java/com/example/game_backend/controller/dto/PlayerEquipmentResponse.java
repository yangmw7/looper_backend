package com.example.game_backend.controller.dto;

import com.example.game_backend.repository.entity.PlayerStats;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerEquipmentResponse {
    private StatsDto stats;
    private Map<String, ItemInfo> equipped;
    private List<ItemInfo> inventory;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatsDto {
        // 최종 스탯 (기본 + 장비)
        private Float hp;
        private Float atk;
        private Float def;
        private Float cri;
        private Float crid;
        private Float spd;
        private Float jmp;
        private Float ats;
        private Integer jcnt;

        // 장비 보너스
        private Float equipmentHp;
        private Float equipmentAtk;
        private Float equipmentDef;
        private Float equipmentCri;
        private Float equipmentCrid;
        private Float equipmentSpd;
        private Float equipmentJmp;
        private Float equipmentAts;
        private Integer equipmentJcnt;

        public static StatsDto from(PlayerStats stats, EquipmentBonus bonus) {
            return StatsDto.builder()
                    .hp(stats.getHp() + bonus.getHp())
                    .atk(stats.getAtk() + bonus.getAtk())
                    .def(stats.getDef() + bonus.getDef())
                    .cri(stats.getCri() + bonus.getCri())
                    .crid(stats.getCrid() + bonus.getCrid())
                    .spd(stats.getSpd() + bonus.getSpd())
                    .jmp(stats.getJmp() + bonus.getJmp())
                    .ats(stats.getAts() + bonus.getAts())
                    .jcnt(stats.getJcnt() + bonus.getJcnt())
                    .equipmentHp(bonus.getHp())
                    .equipmentAtk(bonus.getAtk())
                    .equipmentDef(bonus.getDef())
                    .equipmentCri(bonus.getCri())
                    .equipmentCrid(bonus.getCrid())
                    .equipmentSpd(bonus.getSpd())
                    .equipmentJmp(bonus.getJmp())
                    .equipmentAts(bonus.getAts())
                    .equipmentJcnt(bonus.getJcnt())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquipmentBonus {
        private Float hp;
        private Float atk;
        private Float def;
        private Float cri;
        private Float crid;
        private Float spd;
        private Float jmp;
        private Float ats;
        private Integer jcnt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemInfo {
        private String id;
        private String type;
        private String name;
        private String desc;
        private String icon;        // 이모지 (호환성 유지)
        private String imageUrl;    // 실제 이미지 URL 추가

        // 모든 스탯
        private Float hp;
        private Float atk;
        private Float def;
        private Float cri;
        private Float crid;
        private Float spd;
        private Float jmp;
        private Float ats;
        private Integer jcnt;
    }
}