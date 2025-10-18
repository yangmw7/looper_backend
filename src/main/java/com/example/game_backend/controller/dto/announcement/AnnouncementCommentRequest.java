package com.example.game_backend.controller.dto.announcement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnouncementCommentRequest {
    private String content;
    private Long parentCommentId; // 대댓글용 (null이면 일반 댓글)
}