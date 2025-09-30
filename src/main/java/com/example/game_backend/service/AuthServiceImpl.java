package com.example.game_backend.service;

import com.example.game_backend.config.JwtUtil;
import com.example.game_backend.controller.dto.AuthResponse;
import com.example.game_backend.controller.dto.LoginRequest;
import com.example.game_backend.controller.dto.TokenRefreshRequest;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.RefreshTokenRepository;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.RefreshToken;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;  // 추가

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    // 로그인
    @Override
    @Transactional   // ← delete + save가 하나의 트랜잭션으로 실행되도록 보장
    public AuthResponse login(LoginRequest request) {
        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호 불일치");
        }

        String accessToken = jwtUtil.generateAccessToken(member);
        String refreshToken = jwtUtil.generateRefreshToken(member);

        // 기존 RefreshToken 삭제 후 새로 저장
        refreshTokenRepository.deleteByUsername(member.getUsername());
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .username(member.getUsername())
                        .token(refreshToken)
                        .expiryDate(LocalDateTime.now().plusDays(7))
                        .build()
        );

        return new AuthResponse(
                accessToken,
                refreshToken,
                member.getNickname(),
                List.of(member.getRole().name())
        );
    }

    // 토큰 재발급
    @Override
    @Transactional
    public AuthResponse refresh(TokenRefreshRequest request) {
        String oldRefreshToken = request.getRefreshToken();
        Claims claims = jwtUtil.extractAllClaims(oldRefreshToken);
        String username = claims.getSubject();

        RefreshToken saved = refreshTokenRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (!saved.getToken().equals(oldRefreshToken) || jwtUtil.isTokenExpired(oldRefreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 새 토큰들 발급
        String newAccessToken = jwtUtil.generateAccessToken(member);
        String newRefreshToken = jwtUtil.generateRefreshToken(member);

        // 기존 Refresh Token 삭제 후 새로 저장
        refreshTokenRepository.deleteByUsername(username);
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .username(username)
                        .token(newRefreshToken)
                        .expiryDate(LocalDateTime.now().plusDays(7))
                        .build()
        );

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,   // 새 refresh 반환
                member.getNickname(),
                List.of(member.getRole().name())
        );
    }


    // 로그아웃
    @Override
    @Transactional
    public void logout(String username) {
        refreshTokenRepository.deleteByUsername(username);
    }
}
