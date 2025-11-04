package com.example.game_backend.service;

import com.example.game_backend.controller.dto.*;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.PlayerStatsRepository;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.PlayerStats;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageServiceImpl implements MyPageService {

    private final MemberRepository memberRepository;
    private final PlayerStatsRepository playerStatsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MyPageResponseDto getMyPage(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        PlayerStats stats = playerStatsRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new IllegalArgumentException("게임 스탯을 찾을 수 없습니다"));

        return MyPageResponseDto.builder()
                .profile(ProfileResponseDto.from(member))
                .stats(StatsResponseDto.from(stats))
                .build();
    }

    @Override
    public ProfileResponseDto getProfile(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        return ProfileResponseDto.from(member);
    }

    @Override
    public StatsResponseDto getStats(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        PlayerStats stats = playerStatsRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new IllegalArgumentException("게임 스탯을 찾을 수 없습니다"));

        return StatsResponseDto.from(stats);
    }

    @Override
    @Transactional
    public ProfileResponseDto updateProfile(String username, ProfileUpdateRequestDto requestDto) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        if (!member.getNickname().equals(requestDto.getNickname()) &&
                memberRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다");
        }

        if (!member.getEmail().equals(requestDto.getEmail()) &&
                memberRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
        }

        member.setNickname(requestDto.getNickname());
        member.setEmail(requestDto.getEmail());

        Member updated = memberRepository.save(member);
        return ProfileResponseDto.from(updated);
    }

    @Override
    @Transactional
    public void changePassword(String username, PasswordChangeRequestDto requestDto) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }

        if (!requestDto.getNewPassword().equals(requestDto.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다");
        }

        member.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        memberRepository.save(member);
    }

    // 🆕 PlayerStats 업데이트 (유니티에서 사용)
    @Override
    @Transactional
    public StatsResponseDto updateStats(String username, StatsUpdateRequestDto requestDto) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        PlayerStats stats = playerStatsRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new IllegalArgumentException("게임 스탯을 찾을 수 없습니다"));

        // 값이 있는 필드만 업데이트 (부분 업데이트)
        if (requestDto.getHp() != null) {
            stats.setHp(requestDto.getHp());
        }
        if (requestDto.getAtk() != null) {
            stats.setAtk(requestDto.getAtk());
        }
        if (requestDto.getDef() != null) {
            stats.setDef(requestDto.getDef());
        }
        if (requestDto.getCri() != null) {
            stats.setCri(requestDto.getCri());
        }
        if (requestDto.getCrid() != null) {
            stats.setCrid(requestDto.getCrid());
        }
        if (requestDto.getSpd() != null) {
            stats.setSpd(requestDto.getSpd());
        }
        if (requestDto.getJmp() != null) {
            stats.setJmp(requestDto.getJmp());
        }

        if (requestDto.getClear() != null) {
            stats.setClear(requestDto.getClear());
        }
        if (requestDto.getChapter() != null) {
            stats.setChapter(requestDto.getChapter());
        }
        if (requestDto.getStage() != null) {
            stats.setStage(requestDto.getStage());
        }
        if (requestDto.getMapid() != null) {
            stats.setMapid(requestDto.getMapid());
        }

        if (requestDto.getEquiped() != null) {
            stats.setEquiped(requestDto.getEquiped());
        }
        if (requestDto.getInventory() != null) {
            stats.setInventory(requestDto.getInventory());
        }
        if (requestDto.getAts() != null) {
            stats.setAts(requestDto.getAts());
        }
        if (requestDto.getJcnt() != null) {
            stats.setJcnt(requestDto.getJcnt());
        }

        PlayerStats updated = playerStatsRepository.save(stats);
        return StatsResponseDto.from(updated);
    }
}