package com.example.game_backend.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NpcRequest {
    private String id;
    private List<String> name;
    private float hp;
    private float atk;
    private float def;
    private float spd;
    private List<String> features;
}
