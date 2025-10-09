package com.example.game_backend.repository.entity.report;

import com.example.game_backend.repository.entity.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseReport {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id")
    protected Member reporter;   // 신고자

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_id")
    protected Member reported;   // 피신고자

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "report_reasons", joinColumns = @JoinColumn(name = "report_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 40)
    protected Set<ReasonCode> reasons;

    @Size(max = 100)
    @Column(length = 100)
    protected String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    protected ReportStatus status = ReportStatus.PENDING;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    private String handledBy;
    private LocalDateTime handledAt;
    @Column(length = 200)
    private String handlerMemo;
}