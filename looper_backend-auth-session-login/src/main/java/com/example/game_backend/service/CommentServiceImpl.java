// src/main/java/com/example/game_backend/service/CommentServiceImpl.java
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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
    private final PostRepository postRepository;

    @Override
    @Transactional
    public Comment saveComment(Long postId, CommentRequest commentRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        Comment comment = Comment.builder()
                .member(member)
                .nickname(member.getNickname())
                .post(post)
                .content(commentRequest.getContent())
                .build();

        return commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return commentRepository
                .findAllByPostOrderByCreatedAtDesc(post)
                .stream()
                .map(c -> CommentResponse.builder()
                        .id(c.getId())
                        .content(c.getContent())
                        .writerNickname(c.getNickname())
                        .createdAt(c.getCreatedAt().format(formatter))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Comment updateComment(Long commentId, CommentRequest commentRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. id=" + commentId));

        String writerUsername = comment.getMember().getUsername();
        if (!writerUsername.equals(username)) {
            throw new AccessDeniedException("작성자만 수정할 수 있습니다.");
        }

        comment.setContent(commentRequest.getContent());
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. id=" + commentId));

        String writerUsername = comment.getMember().getUsername();
        if (!writerUsername.equals(username) && !isAdmin) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}
