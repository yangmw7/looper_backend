package com.example.game_backend.service;

import com.example.game_backend.controller.dto.announcement.AnnouncementCommentRequest;
import com.example.game_backend.controller.dto.announcement.AnnouncementCommentResponse;
import java.util.List;

public interface AnnouncementCommentService {
    void saveComment(Long announcementId, AnnouncementCommentRequest request, String username);
    List<AnnouncementCommentResponse> getComments(Long announcementId);
    void updateComment(Long commentId, AnnouncementCommentRequest request, String username);
    void deleteComment(Long commentId, String username);
    void toggleLike(Long commentId, String username);
}