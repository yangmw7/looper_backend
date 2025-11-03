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
        private Float hp;
        private Float atk;
        private Float def;
        private Float cri;
        private Float crid;
        private Float spd;
        private Float jmp;
        private Float ats;
        private Integer jcnt;

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
                    // DB 실제 저장값 그대로
                    .hp(nz(stats.getHp()))
                    .atk(nz(stats.getAtk()))
                    .def(nz(stats.getDef()))
                    .cri(nz(stats.getCri()))
                    .crid(nz(stats.getCrid()))
                    .spd(nz(stats.getSpd()))
                    .jmp(nz(stats.getJmp()))
                    .ats(nz(stats.getAts()))
                    .jcnt(stats.getJcnt() == null ? 0 : stats.getJcnt())

                    // 장비 보너스는 프론트 표시용으로 별도
                    .equipmentHp(nz(bonus.getHp()))
                    .equipmentAtk(nz(bonus.getAtk()))
                    .equipmentDef(nz(bonus.getDef()))
                    .equipmentCri(nz(bonus.getCri()))
                    .equipmentCrid(nz(bonus.getCrid()))
                    .equipmentSpd(nz(bonus.getSpd()))
                    .equipmentJmp(nz(bonus.getJmp()))
                    .equipmentAts(nz(bonus.getAts()))
                    .equipmentJcnt(bonus.getJcnt() == null ? 0 : bonus.getJcnt())
                    .build();
        }

        private static float nz(Float v) {
            return v == null ? 0f : v;
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
        private String icon;
        private String imageUrl;

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
