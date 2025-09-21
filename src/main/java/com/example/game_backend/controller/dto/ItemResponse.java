package com.example.game_backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ItemResponse {
    private String id;
    private List<String> name;
    private String rarity;
    private List<String> description;
    private String attributes;
    private List<String> skills;
}
