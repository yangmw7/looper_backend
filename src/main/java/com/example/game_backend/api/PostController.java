package com.example.game_backend.api;

import com.example.game_backend.controller.dto.PostRequest;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final PasswordEncoder passwordEncoder;
    // 게시글 작성 API

    @PostMapping
    public ResponseEntity<Long> createPost(
            @RequestBody PostRequest postRequest,
            HttpServletRequest request) {

        Member member = (Member) request.getSession().getAttribute("loginMember");

        if (member == null) {
            return ResponseEntity.status(401).build(); // 로그인 안 된 경우
        }

        String writer = member.getNickname(); // ✅ 닉네임 사용
        Long postId = postService.save(postRequest, writer);

        return ResponseEntity.ok(postId);
    }

}
