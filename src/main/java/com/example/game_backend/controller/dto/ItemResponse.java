package com.example.game_backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class ItemResponse {
    private String id;
    private String rarity;
    private boolean twoHander;
    private boolean stackable;
    private List<String> name;
    private List<String> description;
    private List<String> skills;
    private List<AttributeDto> attributes;

    @Getter @Setter
    @AllArgsConstructor
    public static class AttributeDto {
        private String stat;
        private String op;
        private float value;
    }
}
