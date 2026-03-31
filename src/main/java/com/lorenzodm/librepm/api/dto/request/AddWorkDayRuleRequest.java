package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AddWorkDayRuleRequest(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull Boolean isWorkingDay,
        LocalTime startTime,
        LocalTime endTime,
        LocalTime breakStartTime,
        LocalTime breakEndTime
) {}
