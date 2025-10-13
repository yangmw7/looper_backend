package com.example.game_backend.service;

import com.example.game_backend.controller.dto.admin.AppealAdminDto;
import com.example.game_backend.controller.dto.admin.ProcessAppealRequest;
import com.example.game_backend.repository.AppealRepository;
import com.example.game_backend.repository.PenaltyRepository;
import com.example.game_backend.repository.entity.Appeal;
import com.example.game_backend.repository.entity.AppealStatus;
import com.example.game_backend.repository.entity.Penalty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppealAdminServiceImpl implements AppealAdminService {

    private final AppealRepository appealRepository;
    private final PenaltyRepository penaltyRepository;
    private final NotificationService notificationService;

    @Override
    public List<AppealAdminDto> getAllAppeals() {
        return appealRepository.findAllWithPenaltyAndMember().stream()
                .map(AppealAdminDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppealAdminDto> getPendingAppeals() {
        return appealRepository.findByStatusOrderByCreatedAtDesc(AppealStatus.PENDING).stream()
                .map(AppealAdminDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void processAppeal(Long appealId, ProcessAppealRequest request, String adminUsername) {
        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new IllegalArgumentException("이의신청을 찾을 수 없습니다."));

        if (appeal.getStatus() != AppealStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 이의신청입니다.");
        }

        Penalty penalty = appeal.getPenalty();

        if (request.approve()) {
            // 승인: 제재 취소
            appeal.setStatus(AppealStatus.APPROVED);
            penalty.setIsActive(false);

            notificationService.sendAppealApprovedNotification(penalty.getMember());
        } else {
            // 거부: 제재 유지
            appeal.setStatus(AppealStatus.REJECTED);

            notificationService.sendAppealRejectedNotification(penalty.getMember());
        }

        appeal.setAdminResponse(request.adminResponse());
        appeal.setProcessedBy(adminUsername);
        appeal.setProcessedAt(LocalDateTime.now());

        appealRepository.save(appeal);
        penaltyRepository.save(penalty);
    }
}