package com.example.game_backend.controller.dto.report;

import com.example.game_backend.repository.entity.report.ReportStatus;
import jakarta.validation.constraints.NotNull;

public record ReportStatusUpdateRequest(
        @NotNull ReportStatus status,
        String handlerMemo
) {}