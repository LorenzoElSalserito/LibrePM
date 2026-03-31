package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDependencyRequest(
        @NotBlank String predecessorId,
        @NotBlank String successorId,
        @NotNull String type, // FINISH_TO_START, START_TO_START, FINISH_TO_FINISH, START_TO_FINISH
        Integer lag,          // minutes (positive = delay)
        Integer lead          // minutes (positive = overlap/advance)
) {}
