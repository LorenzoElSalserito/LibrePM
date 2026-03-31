package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AddCalendarExceptionRequest(
        @NotNull LocalDate date,
        @NotNull Boolean isWorkingDay,
        @Size(max = 255) String description
) {}
