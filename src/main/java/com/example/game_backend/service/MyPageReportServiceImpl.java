package com.example.game_backend.service;

import com.example.game_backend.controller.dto.mypage.*;
import com.example.game_backend.repository.*;
import com.example.game_backend.repository.PenaltyRepository;
import com.example.game_backend.repository.entity.*;
import com.example.game_backend.repository.report.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageReportServiceImpl implements MyPageReportService {

    private final PenaltyRepository penaltyRepository;
    private final PostReportRepository postReportRepository;
    private final CommentReportRepository commentReportRepository;
    private final MemberRepository memberRepository;
    private final AppealRepository appealRepository;
    private final NotificationService notificationService;

    @Override
    public List<PenaltyDto> getMyPenalties(String username) {
        List<Penalty> penalties = penaltyRepository
                .findByMember_UsernameOrderByCreatedAtDesc(username);

        LocalDateTime now = LocalDateTime.now();
        penalties.forEach(penalty -> {
            if (penalty.getIsActive()
                    && penalty.getEndDate() != null
                    && penalty.getEndDate().isBefore(now)) {
                penalty.setIsActive(false);
            }
        });

        return penalties.stream()
                .map(PenaltyDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<MyReportDto> getMyReports(String username) {
        List<MyReportDto> result = new ArrayList<>();

        postReportRepository.findByReporter_UsernameOrderByCreatedAtDesc(username)
                .forEach(report -> result.add(MyReportDto.fromPostReport(report)));

        commentReportRepository.findByReporter_UsernameOrderByCreatedAtDesc(username)
                .forEach(report -> result.add(MyReportDto.fromCommentReport(report)));

        result.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));

        return result;
    }

    @Override
    @Transactional
    public void submitAppeal(Long penaltyId, String username, String appealReason) {
        Penalty penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new IllegalArgumentException("제재 내역을 찾을 수 없습니다."));

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        if (!penalty.getMember().getId().equals(member.getId())) {
            throw new IllegalStateException("본인의 제재 내역만 이의신청할 수 있습니다.");
        }

        if (!penalty.getCanAppeal()) {
            throw new IllegalStateException("이의신청이 불가능한 제재입니다.");
        }

        if (penalty.getAppealSubmitted()) {
            throw new IllegalStateException("이미 이의신청을 제출하셨습니다.");
        }

        if (appealReason == null || appealReason.trim().isEmpty()) {
            throw new IllegalArgumentException("이의신청 사유를 입력해주세요.");
        }

        // Appeal 엔티티 생성
        Appeal appeal = Appeal.builder()
                .penalty(penalty)
                .appealReason(appealReason.trim())
                .status(AppealStatus.PENDING)
                .build();

        appealRepository.save(appeal);

        // Penalty 상태 업데이트
        penalty.setAppealSubmitted(true);
        penalty.setCanAppeal(false);
        penaltyRepository.save(penalty);

        // 알림 발송
        notificationService.sendAppealReceivedNotification(member);
    }

    @Override
    public Optional<AppealDto> getMyAppeal(Long penaltyId, String username) {
        Penalty penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new IllegalArgumentException("제재 내역을 찾을 수 없습니다."));

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        if (!penalty.getMember().getId().equals(member.getId())) {
            throw new IllegalStateException("본인의 제재 내역만 조회할 수 있습니다.");
        }

        return appealRepository.findByPenaltyId(penaltyId)
                .map(AppealDto::fromEntity);
    }
}