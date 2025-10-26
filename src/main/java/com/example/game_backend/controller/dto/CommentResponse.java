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
    private int likeCount;
    private Long parentCommentId;
    private List<CommentResponse> replies;

    // 추가: 활동내역 조회용
    private Long postId;
    private String postTitle;
}