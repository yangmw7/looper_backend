package com.example.game_backend.api;

import com.example.game_backend.controller.dto.admin.AppealAdminDto;
import com.example.game_backend.controller.dto.admin.ProcessAppealRequest;
import com.example.game_backend.service.AppealAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/appeals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AppealAdminController {

    private final AppealAdminService appealAdminService;

    /**
     * 전체 이의신청 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<AppealAdminDto>> getAllAppeals() {
        List<AppealAdminDto> appeals = appealAdminService.getAllAppeals();
        return ResponseEntity.ok(appeals);
    }

    /**
     * 대기중인 이의신청 목록 조회
     */
    @GetMapping("/pending")
    public ResponseEntity<List<AppealAdminDto>> getPendingAppeals() {
        List<AppealAdminDto> appeals = appealAdminService.getPendingAppeals();
        return ResponseEntity.ok(appeals);
    }

    /**
     * 이의신청 처리 (승인/거부)
     */
    @PostMapping("/{appealId}/process")
    public ResponseEntity<Void> processAppeal(
            @PathVariable Long appealId,
            @RequestBody ProcessAppealRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        appealAdminService.processAppeal(
                appealId,
                request,
                userDetails.getUsername()
        );

        return ResponseEntity.ok().build();
    }
}