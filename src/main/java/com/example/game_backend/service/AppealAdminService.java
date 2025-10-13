package com.example.game_backend.service;

import com.example.game_backend.controller.dto.admin.AppealAdminDto;
import com.example.game_backend.controller.dto.admin.ProcessAppealRequest;

import java.util.List;

public interface AppealAdminService {

    /**
     * 전체 이의신청 목록 조회
     */
    List<AppealAdminDto> getAllAppeals();

    /**
     * 대기중인 이의신청 목록
     */
    List<AppealAdminDto> getPendingAppeals();

    /**
     * 이의신청 처리 (승인/거부)
     */
    void processAppeal(Long appealId, ProcessAppealRequest request, String adminUsername);
}