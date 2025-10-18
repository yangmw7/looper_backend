package com.example.game_backend.repository;

import com.example.game_backend.repository.entity.AnnouncementComment;
import com.example.game_backend.repository.entity.AnnouncementCommentLike;
import com.example.game_backend.repository.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementCommentLikeRepository extends JpaRepository<AnnouncementCommentLike, Long> {
    boolean existsByCommentAndMember(AnnouncementComment comment, Member member);
    void deleteByCommentAndMember(AnnouncementComment comment, Member member);
}