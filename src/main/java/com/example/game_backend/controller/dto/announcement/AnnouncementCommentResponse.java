package com.example.game_backend.controller.dto.announcement;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class AnnouncementCommentResponse {
    private Long id;
    private String content;
    private String writerNickname;
    private Integer likeCount;
    private String createdAt;
    private Long parentCommentId;
    private List<AnnouncementCommentResponse> replies; // 대댓글 목록
}