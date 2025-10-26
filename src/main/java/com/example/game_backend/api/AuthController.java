package com.example.game_backend.api;

import com.example.game_backend.config.JwtUtil;
import com.example.game_backend.controller.dto.AuthResponse;
import com.example.game_backend.controller.dto.DeleteAccountRequest;
import com.example.game_backend.controller.dto.LoginRequest;
import com.example.game_backend.controller.dto.TokenRefreshRequest;
import com.example.game_backend.service.AuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        System.out.println("로그아웃 요청 username = " + username);
        authService.logout(username);
        return ResponseEntity.ok("로그아웃 성공");
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<String> deleteAccount(
            @RequestBody DeleteAccountRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtUtil.extractAllClaims(token);
        String username = claims.getSubject();

        try {
            authService.deleteAccount(username, request.getPassword());
            return ResponseEntity.ok("회원탈퇴가 완료되었습니다");
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}