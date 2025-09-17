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

@Service  // Service 계층으로 등록
@RequiredArgsConstructor // 생성자 주입 자동 생성
public class CommentServiceImpl implements CommentService {

    // 의존성 주입: 댓글, 회원, 게시글 레포지토리
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional // 댓글 저장은 DB 변경이 있으므로 트랜잭션 처리
    public Comment saveComment(Long postId, CommentRequest commentRequest) {
        // 현재 로그인한 사용자명(JWT 기반 SecurityContext에서 가져옴)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 사용자 정보 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));

        // 게시글 정보 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        // 댓글 엔티티 생성
        Comment comment = Comment.builder()
                .member(member)                       // 연관된 사용자
                .nickname(member.getNickname())       // 닉네임 복사 저장 (추후 편의용)
                .post(post)                           // 연결된 게시글
                .content(commentRequest.getContent()) // 댓글 내용
                .build();

        // 댓글 저장
        return commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 최적화
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        // 날짜 포맷 설정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 댓글 리스트 조회 후 CommentResponse DTO로 변환
        return commentRepository
                .findAllByPostOrderByCreatedAtDesc(post) // 최신순 정렬
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
    @Transactional // 수정은 DB 변경이므로 트랜잭션 처리
    public Comment updateComment(Long commentId, CommentRequest commentRequest) {
        // 현재 사용자 이름
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 수정 대상 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. id=" + commentId));

        // 댓글 작성자가 현재 사용자와 다를 경우 예외
        String writerUsername = comment.getMember().getUsername();
        if (!writerUsername.equals(username)) {
            throw new AccessDeniedException("작성자만 수정할 수 있습니다.");
        }

        // 댓글 내용 수정 후 저장
        comment.setContent(commentRequest.getContent());
        return commentRepository.save(comment);
    }

    @Override
    @Transactional // 삭제는 DB 변경이므로 트랜잭션 처리
    public void deleteComment(Long commentId) {
        // 현재 사용자 정보
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // 관리자 권한 여부 확인
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // 삭제 대상 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. id=" + commentId));

        // 작성자 본인 또는 관리자만 삭제 가능
        String writerUsername = comment.getMember().getUsername();
        if (!writerUsername.equals(username) && !isAdmin) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        // 댓글 삭제
        commentRepository.delete(comment);
    }
}
