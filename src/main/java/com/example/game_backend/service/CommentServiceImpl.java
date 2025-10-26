package com.example.game_backend.service;

import com.example.game_backend.controller.dto.CommentRequest;
import com.example.game_backend.controller.dto.CommentResponse;
import com.example.game_backend.repository.CommentLikeRepository;
import com.example.game_backend.repository.CommentRepository;
import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.PostRepository;
import com.example.game_backend.repository.entity.Comment;
import com.example.game_backend.repository.entity.CommentLike;
import com.example.game_backend.repository.entity.Member;
import com.example.game_backend.repository.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentLikeRepository commentLikeRepository;

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

        if (commentRequest.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(commentRequest.getParentCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글 없음"));
            comment.setParentComment(parent);
        }

        return commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return commentRepository
                .findAllByPostAndParentCommentIsNullOrderByCreatedAtDesc(post)
                .stream()
                .map(c -> toResponse(c, formatter))
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

    @Override
    @Transactional
    public void toggleLike(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 없음"));

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        boolean alreadyLiked = commentLikeRepository.existsByCommentAndMember(comment, member);

        if (alreadyLiked) {
            commentLikeRepository.deleteByCommentAndMember(comment, member);
            comment.setLikeCount(comment.getLikeCount() - 1);
        } else {
            CommentLike like = CommentLike.builder()
                    .comment(comment)
                    .member(member)
                    .build();
            commentLikeRepository.save(like);
            comment.setLikeCount(comment.getLikeCount() + 1);
        }

        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Map<String, Object> toggleLikeAndGetStatus(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 없음"));

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        boolean alreadyLiked = commentLikeRepository.existsByCommentAndMember(comment, member);

        if (alreadyLiked) {
            commentLikeRepository.deleteByCommentAndMember(comment, member);
            comment.setLikeCount(comment.getLikeCount() - 1);
        } else {
            CommentLike like = CommentLike.builder()
                    .comment(comment)
                    .member(member)
                    .build();
            commentLikeRepository.save(like);
            comment.setLikeCount(comment.getLikeCount() + 1);
        }

        commentRepository.save(comment);

        boolean isLiked = commentLikeRepository.existsByCommentAndMember(comment, member);

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likeCount", comment.getLikeCount());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByUsername(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        List<Comment> comments = commentRepository.findAllByMemberOrderByCreatedAtDesc(member);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return comments.stream()
                .map(comment -> CommentResponse.builder()
                        .id(comment.getId())
                        .content(comment.getContent())
                        .writerNickname(comment.getNickname())
                        .likeCount(comment.getLikeCount())
                        .createdAt(comment.getCreatedAt().format(formatter))
                        .postId(comment.getPost().getId())
                        .postTitle(comment.getPost().getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    private CommentResponse toResponse(Comment c, DateTimeFormatter formatter) {
        List<CommentResponse> replies = c.getReplies().stream()
                .map(reply -> toResponse(reply, formatter))
                .collect(Collectors.toList());

        return CommentResponse.builder()
                .id(c.getId())
                .content(c.getContent())
                .writerNickname(c.getNickname())
                .likeCount(c.getLikeCount())
                .createdAt(c.getCreatedAt().format(formatter))
                .parentCommentId(c.getParentComment() != null ? c.getParentComment().getId() : null)
                .replies(replies)
                .build();
    }
}