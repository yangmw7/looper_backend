package com.example.game_backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NpcResponse {
    private String id;
    private String name;
    private Float hp;
    private Float atk;
    private Float def;
    private Float spd;
    private List<String> features;
}
