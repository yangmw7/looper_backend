package com.example.game_backend.controller.dto.report;

import com.example.game_backend.repository.entity.report.ReasonCode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record ReportCreateRequest(
        @NotEmpty Set<ReasonCode> reasons,
        @Size(max = 100) String description
) {}