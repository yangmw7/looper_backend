package com.example.game_backend.api;

import com.example.game_backend.config.JwtUtil;
import com.example.game_backend.controller.dto.*;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @GetMapping("/hello")
    public String getHello() {
        return "Hello from Spring Boot!";
    }

    @PostMapping("/api/join")
    public String join(@RequestBody JoinRequest joinRequest){

        String result = memberService.join(joinRequest);

        if("success".equalsIgnoreCase(result)){
            return "회원가입 성공";
        }
        else{
            return "이미 있는 아이디입니다.";
        }
    }

    @PostMapping("/api/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        Member member = memberRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // ✅ JWT 토큰 생성
        String token = jwtUtil.generateToken(member.getUsername());

        // ✅ nickname도 같이 반환
        return ResponseEntity.ok(new LoginResponse(token, member.getNickname()));
    }






    @PostMapping("/api/find-id")
    public String findId(@RequestBody FindIdRequest findIdRequest) {
        Optional<Member> member = memberRepository.findByEmail(findIdRequest.getEmail());
        return member.map(m -> "{\"username\": \"" + m.getUsername() + "\"}")
                .orElse("{\"error\": \"해당 이메일로 가입된 계정이 없습니다.\"}");
    }

    @PostMapping("/api/reset-password/request")
    public String checkResetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest){
        Optional<Member> member = memberService.findByUsernameAndEmail(
                resetPasswordRequest.getUsername(), resetPasswordRequest.getEmail());

        if (member.isPresent()) {
            return "{\"message\": \"확인되었습니다. 비밀번호를 재설정하세요.\"}";
        } else {
            return "{\"error\": \"입력한 정보가 일치하지 않습니다.\"}";
        }
    }

    @PostMapping("/api/reset-password")
    public String resetPassword(@RequestBody ResetPasswordChangeRequest resetPasswordChangeRequest) {
        // 1. 비밀번호 확인 체크
        if (!resetPasswordChangeRequest.getNewPassword().equals(resetPasswordChangeRequest.getConfirmPassword())) {
            return "{\"error\": \"비밀번호와 비밀번호 확인이 일치하지 않습니다.\"}";
        }

        // 2. 비밀번호 변경 시도
        try {
            memberService.updatePassword(resetPasswordChangeRequest.getUsername(), resetPasswordChangeRequest.getNewPassword());
            return "{\"message\": \"비밀번호가 성공적으로 변경되었습니다.\"}";
        } catch (Exception e) {
            return "{\"error\": \"비밀번호 변경에 실패했습니다.\"}";
        }
    }



}