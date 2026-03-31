package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RecordAchievementRequest(
        @NotNull Double achievedValue,
        @NotNull LocalDateTime recordDate,
        @Size(max = 255) String note
) {}
