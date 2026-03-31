package com.lorenzodm.librepm.api.dto.response;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record WorkDayRuleResponse(
        String id,
        String calendarId,
        DayOfWeek dayOfWeek,
        boolean isWorkingDay,
        LocalTime startTime,
        LocalTime endTime,
        LocalTime breakStartTime,
        LocalTime breakEndTime
) {}
