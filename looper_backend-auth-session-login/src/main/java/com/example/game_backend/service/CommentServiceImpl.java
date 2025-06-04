package com.example.game_backend.service;

import com.example.game_backend.controller.dto.CommentRequest;
import com.example.game_backend.controller.dto.CommentResponse;
import com.example.game_backend.repository.CommentRepository;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.PostRepository;
import com.example.game_backend.repository.entity.Comment;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository; // ✅ 추가

    @Override
    public Comment saveComment(Long postId, CommentRequest commentRequest) {
        // 1. JWT로 로그인 사용자 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));

        // 2. 게시글(Post) 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        // 3. 댓글 생성
        Comment comment = Comment.builder()
                .member(member)
                .nickname(member.getNickname()) // ✅ 닉네임도 따로 저장
                .post(post)
                .content(commentRequest.getContent())
                .build();

        // 4. 저장
        return commentRepository.save(comment);
    }

    // (2) 특정 게시물의 댓글 전체 조회
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        // 1) Post 엔티티 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        // 2) CommentRepository를 통해 해당 게시물의 댓글 리스트를 최신순(desc)으로 가져옴
        List<Comment> commentEntities = commentRepository.findAllByPostOrderByCreatedAtDesc(post);

        // 3) CommentResponse DTO로 매핑
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return commentEntities.stream()
                .map(c -> CommentResponse.builder()
                        .id(c.getId())
                        .content(c.getContent())
                        .writerNickname(c.getNickname())
                        .createdAt(c.getCreatedAt().format(formatter))
                        .build()
                )
                .collect(Collectors.toList());
    }

}
