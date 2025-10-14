package com.example.game_backend.api;

import com.example.game_backend.controller.dto.*;
import com.example.game_backend.service.MyPageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 마이페이지 전체 정보 조회
     */
    @GetMapping
    public ResponseEntity<MyPageResponseDto> getMyPage(
            @AuthenticationPrincipal UserDetails userDetails) {
        MyPageResponseDto response = myPageService.getMyPage(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * 프로필 정보만 조회
     */
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDto> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        ProfileResponseDto response = myPageService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * 게임 스탯만 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<StatsResponseDto> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        StatsResponseDto response = myPageService.getStats(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * 프로필 수정 (닉네임, 이메일)
     */
    @PutMapping("/profile")
    public ResponseEntity<ProfileResponseDto> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequestDto requestDto) {
        ProfileResponseDto response = myPageService.updateProfile(
                userDetails.getUsername(), requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 변경
     */
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequestDto requestDto) {
        myPageService.changePassword(userDetails.getUsername(), requestDto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "비밀번호가 변경되었습니다");
        return ResponseEntity.ok(response);
    }

    /**
     * 게임 스탯 업데이트 (유니티에서 사용)
     * PUT /api/mypage/stats
     */
    @PutMapping("/stats")
    public ResponseEntity<StatsResponseDto> updateStats(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody StatsUpdateRequestDto requestDto) {
        StatsResponseDto response = myPageService.updateStats(
                userDetails.getUsername(), requestDto);
        return ResponseEntity.ok(response);
    }
}