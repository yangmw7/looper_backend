package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Announcement;
import com.example.game_backend.repository.entity.AnnouncementComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnnouncementCommentRepository extends JpaRepository<AnnouncementComment, Long> {
    List<AnnouncementComment> findAllByAnnouncementAndParentCommentIsNullOrderByCreatedAtDesc(Announcement announcement);
}