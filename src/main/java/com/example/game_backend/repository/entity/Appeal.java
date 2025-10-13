package com.example.game_backend.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "appeals")
@EntityListeners(AuditingEntityListener.class)
public class Appeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "penalty_id")
    private Penalty penalty;

    @Column(nullable = false, length = 1000)
    private String appealReason; // 이의신청 사유

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AppealStatus status = AppealStatus.PENDING;

    @Column(length = 1000)
    private String adminResponse; // 관리자 답변

    @Column(length = 50)
    private String processedBy; // 처리한 관리자

    private LocalDateTime processedAt; // 처리 일시

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}