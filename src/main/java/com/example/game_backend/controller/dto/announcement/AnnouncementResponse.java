package com.example.game_backend.controller.dto.announcement;

import com.example.game_backend.repository.entity.AnnouncementCategory;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private AnnouncementCategory category;
    private String writer;
    private Integer viewCount;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> imageUrls;
    private Long commentCount;
    private Boolean isPinned;
}