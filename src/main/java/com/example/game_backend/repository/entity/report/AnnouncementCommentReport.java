package com.example.game_backend.repository.entity.report;

import com.example.game_backend.repository.entity.AnnouncementComment;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "announcement_comment_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementCommentReport extends BaseReport {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "announcement_comment_id")
    private AnnouncementComment announcementComment;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "announcement_comment_report_reasons",
            joinColumns = @JoinColumn(name = "announcement_comment_report_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 40)
    private Set<ReasonCode> reasons;
}