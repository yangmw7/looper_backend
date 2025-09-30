package com.example.game_backend.controller.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillResponse {
    private String id;
    private List<String> name;
    private List<String> description;
}
