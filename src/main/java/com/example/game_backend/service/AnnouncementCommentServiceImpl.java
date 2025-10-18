package com.example.game_backend.service;

import com.example.game_backend.controller.dto.announcement.AnnouncementCommentRequest;
import com.example.game_backend.controller.dto.announcement.AnnouncementCommentResponse;
import com.example.game_backend.repository.*;
import com.example.game_backend.repository.entity.*;
import com.example.game_backend.security.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementCommentServiceImpl implements AnnouncementCommentService {

    private final AnnouncementCommentRepository commentRepository;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementCommentLikeRepository commentLikeRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void saveComment(Long announcementId, AnnouncementCommentRequest request, String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("공지사항 없음"));

        AnnouncementComment comment = AnnouncementComment.builder()
                .announcement(announcement)
                .member(member)
                .nickname(member.getNickname())
                .content(request.getContent())
                .build();

        if (request.getParentCommentId() != null) {
            AnnouncementComment parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("부모 댓글 없음"));
            comment.setParentComment(parent);
        }

        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementCommentResponse> getComments(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("공지사항 없음"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return commentRepository
                .findAllByAnnouncementAndParentCommentIsNullOrderByCreatedAtDesc(announcement)
                .stream()
                .map(c -> toResponse(c, formatter))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateComment(Long commentId, AnnouncementCommentRequest request, String username) {
        AnnouncementComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글 없음"));

        if (!comment.getMember().getUsername().equals(username)) {
            throw new AccessDeniedException("작성자만 수정할 수 있습니다.");
        }

        comment.setContent(request.getContent());
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String username) {
        AnnouncementComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글 없음"));

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        boolean isAdmin = member.getRole() == Role.ADMIN;
        boolean isOwner = comment.getMember().getUsername().equals(username);

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void toggleLike(Long commentId, String username) {
        AnnouncementComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글 없음"));

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        boolean alreadyLiked = commentLikeRepository.existsByCommentAndMember(comment, member);

        if (alreadyLiked) {
            commentLikeRepository.deleteByCommentAndMember(comment, member);
            comment.setLikeCount(comment.getLikeCount() - 1);
        } else {
            AnnouncementCommentLike like = AnnouncementCommentLike.builder()
                    .comment(comment)
                    .member(member)
                    .build();
            commentLikeRepository.save(like);
            comment.setLikeCount(comment.getLikeCount() + 1);
        }

        commentRepository.save(comment);
    }

    private AnnouncementCommentResponse toResponse(AnnouncementComment c, DateTimeFormatter formatter) {
        List<AnnouncementCommentResponse> replies = c.getReplies().stream()
                .map(reply -> toResponse(reply, formatter))
                .collect(Collectors.toList());

        return AnnouncementCommentResponse.builder()
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