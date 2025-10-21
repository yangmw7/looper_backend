package com.example.game_backend.controller.dto;

import com.example.game_backend.repository.entity.PlayerStats;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsResponseDto {

    private Float hp;
    private Float atk;
    private Float def;
    private Float cri;
    private Float crid;
    private Float spd;
    private Float jmp;
    private Float ats;
    private Integer jcnt;
    private String skills;

    private Integer clear;
    private Integer chapter;
    private Integer stage;
    private String mapid;

    private String equiped;
    private String inventory;

    // Entity -> DTO 변환
    public static StatsResponseDto from(PlayerStats stats) {
        return StatsResponseDto.builder()
                .hp(stats.getHp())
                .atk(stats.getAtk())
                .def(stats.getDef())
                .cri(stats.getCri())
                .crid(stats.getCrid())
                .spd(stats.getSpd())
                .jmp(stats.getJmp())
                .ats(stats.getAts())
                .jcnt(stats.getJcnt())
                .skills(stats.getSkills())
                .clear(stats.getClear())
                .chapter(stats.getChapter())
                .stage(stats.getStage())
                .mapid(stats.getMapid())
                .equiped(stats.getEquiped())
                .inventory(stats.getInventory())
                .build();
    }
}