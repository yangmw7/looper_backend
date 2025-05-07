package com.example.game_backend.controller;

import com.example.game_backend.controller.dto.JoinRequest;
import com.example.game_backend.controller.dto.LoginRequest;
import com.example.game_backend.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MemberViewController {

    private final MemberService memberService;

    // 회원가입 화면
    @GetMapping("/join-form")
    public String joinForm() {
        return "join"; // templates/join.mustache
    }

    // 회원가입 처리 (DTO 사용!)
    @PostMapping("/join-form")
    public String joinSubmit(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String email) {

        JoinRequest joinRequest = new JoinRequest(username, password, email);
        memberService.join(joinRequest);

        return "redirect:/join-success";
    }

    // 로그인 화면
    @GetMapping("/login-form")
    public String loginForm() {
        return "login"; // templates/login.mustache
    }

    // 로그인 처리
    @PostMapping("/login-form")
    public String loginSubmit(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session) {

        LoginRequest loginRequest = new LoginRequest(username, password);
        boolean result = memberService.login(loginRequest);

        if (result) {
            session.setAttribute("username", username);
            return "redirect:/home";
        } else {
            return "redirect:/login-form?error";
        }
    }

    // 로그인 후 홈
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login-form";
        }
        model.addAttribute("username", username);
        return "home";
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login-form";
    }

    @GetMapping("/find-id")
    public String findIdForm() {
        return "find-id"; // templates/find-id.mustache
    }

    @PostMapping("/find-id")
    public String findIdSubmit(@RequestParam String email, Model model) {
        String username = memberService.findUsernameByEmail(email);
        model.addAttribute("result", username != null ? username : "해당 이메일로 가입된 계정이 없습니다.");
        return "find-id-result";
    }

    @GetMapping("/join-success")
    public String joinSuccess() {
        return "join-success"; // templates/join-success.mustache
    }


}
