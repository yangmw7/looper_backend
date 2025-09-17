// src/main/java/com/example/game_backend/controller/dto/PostResponse.java
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> imageUrls;

    // ↓ 댓글 수 필드 추가
    private long commentCount;
}
