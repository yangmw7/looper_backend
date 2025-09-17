package com.example.game_backend.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommentResponse{
    private Long id;
    private String content;
    private String writerNickname;
    private String createdAt;
}
