package com.example.game_backend.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class ItemRequest {
    @NotBlank(message = "아이템 ID는 필수입니다.")
    private String id;

    @NotBlank(message = "레어리티는 필수입니다.")
    private String rarity;

    private boolean twoHander;
    private boolean stackable;

    @NotEmpty(message = "이름은 최소 1개 이상 필요합니다.")
    private List<@Size(min = 1, max = 50, message = "이름은 1~50자여야 합니다.") String> name;

    @NotEmpty(message = "설명은 최소 1개 이상 필요합니다.")
    private List<String> description;

    private List<String> skills;

    @NotEmpty(message = "속성은 최소 1개 이상 필요합니다.")
    private List<AttributeDto> attributes;

    // 이미지 URL (업로드 후 자동으로 설정됨)
    private String imageUrl;

    @Getter @Setter
    public static class AttributeDto {
        @NotBlank(message = "stat은 필수입니다.")
        private String stat;

        @NotBlank(message = "op는 필수입니다.")
        private String op;

        private float value;
    }
}