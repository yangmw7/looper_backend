package com.example.game_backend.controller.dto;

import com.example.game_backend.repository.entity.Member;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MemberResponse {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private LocalDateTime createdDate;

}
