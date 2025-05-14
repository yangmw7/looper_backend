package com.example.game_backend.controller.dto;


import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String writer;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
