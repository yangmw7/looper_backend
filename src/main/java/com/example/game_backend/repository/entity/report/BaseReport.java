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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    // 신고자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id")
    protected Member reporter;

    // 피신고자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_id")
    protected Member reported;

    // 신고 사유들 (복수 선택 가능)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "report_reasons", joinColumns = @JoinColumn(name = "report_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 40)
    protected Set<ReasonCode> reasons;

    // 상세 설명
    @Size(max = 500)
    @Column(length = 500)
    protected String description;

    // 신고 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    protected ReportStatus status = ReportStatus.PENDING;

    // 신고 접수 시간
    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    // 처리한 관리자
    @Column(length = 50)
    private String handledBy;

    // 처리 시간
    private LocalDateTime handledAt;

    // 관리자 메모
    @Column(length = 500)
    private String handlerMemo;
}