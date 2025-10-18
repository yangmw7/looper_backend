package com.example.game_backend.repository.entity.report;

import com.example.game_backend.repository.entity.AnnouncementComment;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "announcement_comment_reports")
public class AnnouncementCommentReport extends BaseReport {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "announcement_comment_id")
    private AnnouncementComment announcementComment;
}