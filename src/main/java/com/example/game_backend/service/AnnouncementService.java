package com.example.game_backend.service;

import com.example.game_backend.controller.dto.announcement.AnnouncementRequest;
import com.example.game_backend.controller.dto.announcement.AnnouncementResponse;
import com.example.game_backend.repository.entity.AnnouncementCategory;
import com.example.game_backend.repository.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface AnnouncementService {
    Long save(AnnouncementRequest request, Member writer);
    void updateAnnouncement(Long id, AnnouncementRequest request);
    void deleteAnnouncement(Long id);
    String storeFileAndGetUrl(MultipartFile file);
    Page<AnnouncementResponse> getAll(AnnouncementCategory category, Pageable pageable);
    AnnouncementResponse getOne(Long id);
    void toggleLike(Long announcementId, String username);
    void togglePin(Long announcementId);
}