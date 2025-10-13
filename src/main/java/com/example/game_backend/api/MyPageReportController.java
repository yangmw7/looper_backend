package com.example.game_backend.api;

import com.example.game_backend.controller.dto.mypage.*;
import com.example.game_backend.service.MyPageReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageReportController {

    private final MyPageReportService myPageReportService;

    @GetMapping("/penalties")
    public ResponseEntity<List<PenaltyDto>> getMyPenalties(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<PenaltyDto> penalties = myPageReportService.getMyPenalties(
                userDetails.getUsername()
        );

        return ResponseEntity.ok(penalties);
    }

    @GetMapping("/reports")
    public ResponseEntity<List<MyReportDto>> getMyReports(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<MyReportDto> reports = myPageReportService.getMyReports(
                userDetails.getUsername()
        );

        return ResponseEntity.ok(reports);
    }

    /**
     * 제재에 대한 이의신청 (사유 포함)
     */
    @PostMapping("/penalties/{penaltyId}/appeal")
    public ResponseEntity<Void> submitAppeal(
            @PathVariable Long penaltyId,
            @RequestBody SubmitAppealRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        myPageReportService.submitAppeal(
                penaltyId,
                userDetails.getUsername(),
                request.appealReason()
        );

        return ResponseEntity.ok().build();
    }

    /**
     * 내 이의신청 조회
     */
    @GetMapping("/penalties/{penaltyId}/appeal")
    public ResponseEntity<AppealDto> getMyAppeal(
            @PathVariable Long penaltyId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return myPageReportService.getMyAppeal(penaltyId, userDetails.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}