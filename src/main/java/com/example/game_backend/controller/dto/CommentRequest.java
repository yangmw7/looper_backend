package com.example.game_backend.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
    private String content;

    // 대댓글 작성 시 부모 댓글 ID
    private Long parentCommentId;
}