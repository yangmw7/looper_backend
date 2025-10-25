package com.example.game_backend.repository.entity.report;

import com.example.game_backend.repository.entity.Comment;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "comment_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReport extends BaseReport {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "comment_report_reasons",  // ⭐ 별도 테이블명
            joinColumns = @JoinColumn(name = "comment_report_id")  // ⭐ 별도 컬럼명
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 40)
    private Set<ReasonCode> reasons;
}