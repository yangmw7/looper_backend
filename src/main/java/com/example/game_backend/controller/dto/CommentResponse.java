package com.example.game_backend.controller.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String writerNickname;
    private String createdAt;

    // 좋아요 수
    private int likeCount;

    // 대댓글 정보
    private Long parentCommentId;
    private List<CommentResponse> replies;
}