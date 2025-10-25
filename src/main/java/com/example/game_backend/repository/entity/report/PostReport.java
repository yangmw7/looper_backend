package com.example.game_backend.repository.entity.report;

import com.example.game_backend.repository.entity.Post;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "post_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReport extends BaseReport {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "post_report_reasons",
            joinColumns = @JoinColumn(name = "post_report_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 40)
    private Set<ReasonCode> reasons;
}