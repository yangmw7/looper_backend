package com.example.game_backend.controller;

import com.example.game_backend.controller.dto.ResetPasswordRequest;
import com.example.game_backend.controller.dto.ResetPasswordChangeRequest;
import com.example.game_backend.service.PasswordService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @GetMapping("/find-password")
    public String findPasswordForm() {
        return "find-password";
    }

    @PostMapping("/find-password")
    public String findPassword(@ModelAttribute ResetPasswordRequest request, Model model, HttpSession session) {
        boolean exists = passwordService.verifyUser(request);
        if (exists) {
            session.setAttribute("resetUsername", request.getUsername());
            return "redirect:/reset-password";
        } else {
            model.addAttribute("error", "일치하는 사용자가 없습니다.");
            return "find-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(HttpSession session, Model model) {
        if (session.getAttribute("resetUsername") == null) {
            return "redirect:/find-password";
        }
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@ModelAttribute ResetPasswordChangeRequest request, Model model, HttpSession session) {
        String username = (String) session.getAttribute("resetUsername");

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "reset-password";
        }

        passwordService.resetPassword(username, request.getNewPassword());
        session.removeAttribute("resetUsername");
        return "reset-success";
    }
}
