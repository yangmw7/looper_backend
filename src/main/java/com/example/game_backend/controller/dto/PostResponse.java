package com.example.game_backend.controller.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String writer;
    private int viewCount;

    // 좋아요 수
    private int likeCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> imageUrls;
    private long commentCount;
}