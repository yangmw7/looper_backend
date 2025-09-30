package com.example.game_backend.api;

import com.example.game_backend.controller.dto.AuthResponse;
import com.example.game_backend.controller.dto.LoginRequest;
import com.example.game_backend.controller.dto.TokenRefreshRequest;
import com.example.game_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        System.out.println("🔎 로그아웃 요청 username = " + username);
        authService.logout(username);
        return ResponseEntity.ok("로그아웃 성공");
    }


}
