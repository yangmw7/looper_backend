package com.example.game_backend.service;

import com.example.game_backend.controller.dto.mypage.*;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 마이페이지: 제재 내역 & 신고 내역 조회
 */
public interface MyPageReportService {

    /**
     * 내가 받은 제재 목록
     */
    List<PenaltyDto> getMyPenalties(String username);

    /**
     * 내가 신고한 내역
     */
    List<MyReportDto> getMyReports(String username);

    /**
     * 제재에 대한 이의신청
     */
    void submitAppeal(Long penaltyId, String username, String appealReason);

    /**
     * 내 이의신청 조회
     */
    Optional<AppealDto> getMyAppeal(Long penaltyId, String username);
}
