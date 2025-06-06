package com.example.game_backend.api;

import com.example.game_backend.controller.dto.PostRequest;
import com.example.game_backend.controller.dto.PostResponse;
import com.example.game_backend.repository.entity.Image;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.Post;
import com.example.game_backend.repository.PostRepository;
import com.example.game_backend.repository.MemberRepository;
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

    // ✅ 1. 게시글 작성 (여러 이미지 첨부)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createPost(
            @ModelAttribute PostRequest postRequest,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        String writer = member.getNickname();
        Long postId = postService.save(postRequest, writer);
        return ResponseEntity.ok(postId);
    }

    // ✅ 2. 게시글 목록 조회 (이미지 URL 리스트 포함)
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<PostResponse> posts = postRepository.findAll().stream()
                .map(post -> {
                    List<String> urls = post.getImages().stream()
                            .map(image -> image.getFilePath())
                            .collect(Collectors.toList());

                    return PostResponse.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .writer(post.getWriter())
                            .viewCount(post.getViewCount())
                            .createdAt(post.getCreatedAt())
                            .updatedAt(post.getUpdatedAt())
                            .imageUrls(urls)
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(posts);
    }

    // ✅ 3. 게시글 단건 조회 (이미지 URL 리스트 포함 + 조회수 증가)
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        // 조회수 증가
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        List<String> urls = post.getImages().stream()
                .map(image -> image.getFilePath())
                .collect(Collectors.toList());

        PostResponse response = PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writer(post.getWriter())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .imageUrls(urls)
                .build();

        return ResponseEntity.ok(response);
    }

    // ✅ 4. 게시글 삭제 (이미지도 Cascade로 함께 삭제됨)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // src/main/java/com/example/game_backend/api/PostController.java
// … (중략) …

    // ✅ 5. 게시글 수정 (제목/내용 + 이미지 교체 혹은 추가)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updatePost(
            @PathVariable Long id,
            @ModelAttribute PostRequest postRequest,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        if (!post.getWriter().equals(member.getNickname())) {
            return ResponseEntity.status(403).body("수정 권한 없음");
        }

        // 1) 제목·내용 업데이트
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());

        // 2) 이미지가 새로 업로드된 경우 (MultipartFile[] 배열로 변경)
        MultipartFile[] newFiles = postRequest.getImageFiles();
        if (newFiles != null && newFiles.length > 0) {
            // (a) 기존 이미지 모두 삭제 (orphanRemoval = true 덕분에 DB에서 자동 삭제)
            post.getImages().clear();

            // (b) 새로 받은 파일들 저장 → Image 엔티티로 만들어 post에 추가
            for (MultipartFile file : newFiles) {
                if (file != null && !file.isEmpty()) {
                    String url = postService.storeFileAndGetUrl(file);
                    Image imgEntity = Image.builder()
                            .filePath(url)
                            .post(post)
                            .build();
                    post.getImages().add(imgEntity);
                }
            }
        }
        // 3) 변경된 post 저장 (Cascade.ALL 덕분에 Image들도 함께 저장됨)
        postRepository.save(post);

        return ResponseEntity.ok("수정 완료");
    }

}
