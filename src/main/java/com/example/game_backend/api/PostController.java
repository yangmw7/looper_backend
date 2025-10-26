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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAll() {
        List<PostResponse> list = postRepository.findAll().stream()
                .map(post -> {
                    List<String> urls = post.getImages().stream()
                            .map(Image::getFilePath)
                            .collect(Collectors.toList());
                    long commentCount = post.getComments().size();
                    return PostResponse.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .writer(post.getWriter().getNickname())
                            .viewCount(post.getViewCount())
                            .likeCount(post.getLikeCount())
                            .createdAt(post.getCreatedAt())
                            .updatedAt(post.getUpdatedAt())
                            .imageUrls(urls)
                            .commentCount(commentCount)
                            .build();
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getOne(@PathVariable Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        List<String> urls = post.getImages().stream()
                .map(Image::getFilePath)
                .collect(Collectors.toList());
        long commentCount = post.getComments().size();

        PostResponse res = PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writer(post.getWriter().getNickname())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .imageUrls(urls)
                .commentCount(commentCount)
                .build();
        return ResponseEntity.ok(res);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Post existing = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        // 본인 또는 관리자만 삭제 가능
        if (!existing.getWriter().getId().equals(member.getId()) &&
                !member.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }

        postService.deletePost(id);
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
        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Post existing = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        if (!existing.getWriter().getId().equals(member.getId())) {
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

    // ⭐ 게시글 좋아요 토글 (Service만 호출)
    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> togglePostLike(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);

        // ✅ Service에서 상태를 받아옴
        Map<String, Object> response = postService.toggleLikeAndGetStatus(id, username);
        return ResponseEntity.ok(response);
    }

    // ⭐ 게시글 좋아요 상태 조회 (Service만 호출)
    @GetMapping("/{id}/like/status")
    public ResponseEntity<Map<String, Object>> getPostLikeStatus(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);

        // ✅ Service에서 상태를 받아옴
        boolean isLiked = postService.isPostLikedByUser(id, username);

        Map<String, Object> response = new HashMap<>();
        response.put("isLiked", isLiked);

        return ResponseEntity.ok(response);
    }

    // ⭐ 댓글 좋아요 상태 조회 (Service만 호출)
    @GetMapping("/{id}/comments/likes/status")
    public ResponseEntity<Map<String, Object>> getCommentLikesStatus(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);

        // ✅ Service에서 상태를 받아옴
        List<Long> likedCommentIds = postService.getLikedCommentIds(id, username);

        Map<String, Object> response = new HashMap<>();
        response.put("likedCommentIds", likedCommentIds);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-posts")
    public ResponseEntity<List<PostResponse>> getMyPosts(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);

        List<PostResponse> myPosts = postService.getMyPosts(username);

        return ResponseEntity.ok(myPosts);
    }
}