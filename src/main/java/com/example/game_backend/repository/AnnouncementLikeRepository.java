package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.Announcement;
import com.example.game_backend.repository.entity.AnnouncementLike;
import com.example.game_backend.repository.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementLikeRepository extends JpaRepository<AnnouncementLike, Long> {
    boolean existsByAnnouncementAndMember(Announcement announcement, Member member);
    void deleteByAnnouncementAndMember(Announcement announcement, Member member);
}