package com.example.game_backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PlayerStats 업데이트 요청 DTO
 * 유니티에서 게임 플레이 중 변경사항을 전송할 때 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StatsUpdateRequestDto {

    // 스탯 정보
    private Float hp;
    private Float atk;
    private Float def;
    private Float cri;
    private Float crid;
    private Float spd;
    private Float jmp;

    // 진행도 정보
    private Integer clear;
    private Integer chapter;
    private Integer stage;
    private String mapid;

    // 장비/인벤토리 (JSON 문자열)
    private String equiped;
    private String inventory;
}