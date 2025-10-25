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
import java.util.Collections;
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
        // 공지사항 댓글 작성 차단
        throw new UnsupportedOperationException("공지사항에는 댓글을 작성할 수 없습니다.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementCommentResponse> getComments(Long announcementId) {
        // 공지사항은 댓글이 없으므로 빈 리스트 반환
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public void updateComment(Long commentId, AnnouncementCommentRequest request, String username) {
        // 공지사항 댓글 수정 차단
        throw new UnsupportedOperationException("공지사항 댓글은 수정할 수 없습니다.");
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String username) {
        // 공지사항 댓글 삭제 차단
        throw new UnsupportedOperationException("공지사항 댓글은 삭제할 수 없습니다.");
    }

    @Override
    @Transactional
    public void toggleLike(Long commentId, String username) {
        // 공지사항 댓글 좋아요 차단
        throw new UnsupportedOperationException("공지사항 댓글에는 좋아요를 할 수 없습니다.");
    }
}