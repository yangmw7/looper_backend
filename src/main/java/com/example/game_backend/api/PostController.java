// src/main/java/com/example/game_backend/api/PostController.java
package com.example.game_backend.api;

import com.example.game_backend.controller.dto.PostRequest;
import com.example.game_backend.controller.dto.PostResponse;
import com.example.game_backend.repository.entity.Image;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.Post;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.PostRepository;
import com.example.game_backend.service.PostService;
import com.example.game_backend.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    // 게시글 작성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createPost(
            @ModelAttribute PostRequest postRequest,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Long id = postService.save(postRequest, member);
        return ResponseEntity.ok(id);
    }

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAll() {
        List<PostResponse> list = postRepository.findAll().stream()
                .map(post -> {
                    List<String> urls = post.getImages().stream()
                            .map(Image::getFilePath)
                            .collect(Collectors.toList());
                    long commentCount = post.getComments().size();  // 댓글 수
                    return PostResponse.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .writer(post.getWriter().getNickname())
                            .viewCount(post.getViewCount())
                            .createdAt(post.getCreatedAt())
                            .updatedAt(post.getUpdatedAt())
                            .imageUrls(urls)
                            .commentCount(commentCount)                 // ← 추가
                            .build();
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // 게시글 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getOne(@PathVariable Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        List<String> urls = post.getImages().stream()
                .map(Image::getFilePath)
                .collect(Collectors.toList());
        long commentCount = post.getComments().size();      // 댓글 수

        PostResponse res = PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writer(post.getWriter().getNickname())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .imageUrls(urls)
                .commentCount(commentCount)                     // ← 추가
                .build();
        return ResponseEntity.ok(res);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestHeader("Authorization") String authHeader) {
        postRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 게시글 수정
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updatePost(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "keepImageUrls", required = false) List<String> keepImageUrls,
            @RequestPart(value = "imageFiles", required = false) MultipartFile[] imageFiles,
            @RequestHeader("Authorization") String authHeader
    ) {
        // 본인 확인
        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Post existing = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));
        if (!existing.getWriter().equals(member.getNickname())) {
            return ResponseEntity.status(403).body("권한 없음");
        }

        PostRequest req = new PostRequest();
        req.setTitle(title);
        req.setContent(content);
        req.setKeepImageUrls(keepImageUrls);
        req.setImageFiles(imageFiles);

        postService.updatePost(id, req);
        return ResponseEntity.ok("수정 완료");
    }
}
