package com.example.game_backend.api;

import com.example.game_backend.controller.dto.FindIdRequest;
import com.example.game_backend.controller.dto.JoinRequest;
import com.example.game_backend.controller.dto.ResetPasswordChangeRequest;
import com.example.game_backend.controller.dto.ResetPasswordRequest;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @PostMapping("/api/join")
    public String join(@RequestBody JoinRequest joinRequest) {
        return memberService.join(joinRequest);
    }

    // 로그인 제거 (AuthController에서 담당)

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
