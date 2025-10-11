package com.example.game_backend.controller.dto;

import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.security.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseDto {

    private String username;
    private String email;
    private String nickname;
    private Role role;
    private LocalDateTime createdDate;

    // Entity -> DTO 변환
    public static ProfileResponseDto from(Member member) {
        return ProfileResponseDto.builder()
                .username(member.getUsername())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .role(member.getRole())
                .createdDate(member.getCreatedDate())
                .build();
    }
}