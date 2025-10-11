package com.example.game_backend.service;

import com.example.game_backend.controller.dto.PasswordChangeRequestDto;
import com.example.game_backend.controller.dto.ProfileUpdateRequestDto;
import com.example.game_backend.controller.dto.MyPageResponseDto;
import com.example.game_backend.controller.dto.ProfileResponseDto;
import com.example.game_backend.controller.dto.StatsResponseDto;

public interface MyPageService {

    // 마이페이지 전체 정보 조회
    MyPageResponseDto getMyPage(String username);

    // 프로필 정보만 조회
    ProfileResponseDto getProfile(String username);

    // 게임 스탯만 조회
    StatsResponseDto getStats(String username);

    // 프로필 수정 (닉네임, 이메일)
    ProfileResponseDto updateProfile(String username, ProfileUpdateRequestDto requestDto);

    // 비밀번호 변경
    void changePassword(String username, PasswordChangeRequestDto requestDto);
}