package com.example.game_backend.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
    private Long MemberId;     // 어느 게시글에 다는 댓글인지
    private String content;     // 댓글 내용
}
