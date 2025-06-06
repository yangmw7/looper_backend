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
    private final PostRepository postRepository;

    @Override
    @Transactional
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
                .nickname(member.getNickname())
                .post(post)
                .content(commentRequest.getContent())
                .build();

        // 4. 저장
        return commentRepository.save(comment);
    }

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

    // ── 여기부터 수정·삭제 메서드 추가 ──

    @Override
    @Transactional
    public Comment updateComment(Long commentId, CommentRequest commentRequest) {
        // 1. 로그인한 사용자 username
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. 수정할 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. id=" + commentId));

        // 3. 작성자 검사 (로그인한 사람과 댓글 작성자가 같아야 수정 가능)
        String writerUsername = comment.getMember().getUsername();
        if (!writerUsername.equals(username)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        // 4. 내용 변경
        comment.setContent(commentRequest.getContent());
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        // 1. 로그인한 사용자 username
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. 삭제할 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. id=" + commentId));

        // 3. 작성자 검사
        String writerUsername = comment.getMember().getUsername();
        if (!writerUsername.equals(username)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        // 4. 삭제
        commentRepository.delete(comment);
    }
}
