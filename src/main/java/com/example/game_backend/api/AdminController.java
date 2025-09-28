    package com.example.game_backend.api;

    import com.example.game_backend.controller.dto.CommentResponse;
    import com.example.game_backend.controller.dto.MemberResponse;
    import com.example.game_backend.controller.dto.PostResponse;
    import com.example.game_backend.service.AdminService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/api/admin")
    @RequiredArgsConstructor
    public class AdminController {
        private final AdminService adminService;

        @GetMapping("/users")
        public List<MemberResponse> listUsers() {
            return adminService.getAllUsers().stream()
                    .map(member -> MemberResponse.builder()
                            .id(member.getId())
                            .username(member.getUsername())
                            .nickname(member.getNickname())
                            .role(member.getRole().name())
                            .createdDate(member.getCreatedDate())
                            .build()
                    )
                    .toList();
        }

        @DeleteMapping("/users/{id}")
        public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
            adminService.deleteUser(id);
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/posts")
        public List<PostResponse> listPosts() {
            return adminService.getAllPosts().stream()
                    .map(post -> PostResponse.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .writer(post.getWriter())
                            .viewCount(post.getViewCount())
                            .createdAt(post.getCreatedAt())
                            .updatedAt(post.getUpdatedAt())
                            // imageUrls, commentCount는 필요 시 서비스에서 채워 넣을 수 있음
                            .build()
                    )
                    .toList();
        }

        @DeleteMapping("/posts/{id}")
        public ResponseEntity<Void> deletePost(@PathVariable Long id) {
            adminService.deletePost(id);
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/comments")
        public List<CommentResponse> listComments() {
            return adminService.getAllComments().stream()
                    .map(comment -> CommentResponse.builder()
                            .id(comment.getId())
                            .content(comment.getContent())
                            .writerNickname(comment.getMember().getNickname())
                            .createdAt(comment.getCreatedAt().toString()) // DTO에서 String 타입이므로 변환
                            .build()
                    )
                    .toList();
        }

        @DeleteMapping("/comments/{id}")
        public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
            adminService.deleteComment(id);
            return ResponseEntity.noContent().build();
        }
    }
