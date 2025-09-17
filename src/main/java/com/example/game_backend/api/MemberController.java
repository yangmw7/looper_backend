package com.example.game_backend.api;

import com.example.game_backend.config.JwtUtil;
import com.example.game_backend.controller.dto.FindIdRequest;
import com.example.game_backend.controller.dto.JoinRequest;
import com.example.game_backend.controller.dto.LoginRequest;
import com.example.game_backend.controller.dto.LoginResponse;
import com.example.game_backend.controller.dto.ResetPasswordChangeRequest;
import com.example.game_backend.controller.dto.ResetPasswordRequest;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/hello")
    public String getHello() {
        return "Hello from Spring Boot!";
    }

    @PostMapping("/api/join")
    public String join(@RequestBody JoinRequest joinRequest) {
        String result = memberService.join(joinRequest);
        if ("success".equalsIgnoreCase(result)) {
            return "회원가입 성공";
        } else if ("fail_email".equalsIgnoreCase(result)) {
            return "이미 있는 이메일입니다.";
        } else {
            return "이미 있는 아이디입니다.";
        }
    }

    @PostMapping("/api/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        // [1] 서비스에서 로그인 처리 (아이디/비번 확인 후 Optional<Member> 반환)
        Optional<Member> optionalMember = memberService.login(loginRequest);

        // [2] 로그인 실패 시 401 Unauthorized 응답
        if (optionalMember.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // [3] 로그인 성공 시 Member 객체 꺼냄
        Member member = optionalMember.get();

        // [4] JWT 토큰 생성 (username, role 등을 기반으로)
        String token = jwtUtil.generateToken(member);

        // [5] 사용자 권한을 문자열로 리스트화
        List<String> roles = List.of(member.getRole().name());

        // [6] 응답에 엔티티 직접 반환 ❌ → DTO(LoginResponse)로 변환해서 응답 ✅
        return ResponseEntity.ok(new LoginResponse(token, member.getNickname(), roles));
    }





    @PostMapping("/api/find-id")
    public String findId(@RequestBody FindIdRequest findIdRequest) {
        Optional<Member> member = memberRepository.findByEmail(findIdRequest.getEmail());
        return member
                .map(m -> "{\"username\": \"" + m.getUsername() + "\"}")
                .orElse("{\"error\": \"해당 이메일로 가입된 계정이 없습니다.\"}");
    }

    @PostMapping("/api/reset-password/request")
    public String checkResetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        Optional<Member> member = memberService.findByUsernameAndEmail(
                resetPasswordRequest.getUsername(),
                resetPasswordRequest.getEmail()
        );
        if (member.isPresent()) {
            return "{\"message\": \"확인되었습니다. 비밀번호를 재설정하세요.\"}";
        } else {
            return "{\"error\": \"입력한 정보가 일치하지 않습니다.\"}";
        }
    }

    @PostMapping("/api/reset-password")
    public String resetPassword(@RequestBody ResetPasswordChangeRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            return "{\"error\": \"비밀번호와 비밀번호 확인이 일치하지 않습니다.\"}";
        }
        try {
            memberService.updatePassword(req.getUsername(), req.getNewPassword());
            return "{\"message\": \"비밀번호가 성공적으로 변경되었습니다.\"}";
        } catch (Exception e) {
            return "{\"error\": \"비밀번호 변경에 실패했습니다.\"}";
        }
    }
}
