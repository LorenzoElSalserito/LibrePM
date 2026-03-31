package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;
import java.util.List;

public record WorkCalendarResponse(
        String id,
        String name,
        String description,
        List<WorkDayRuleResponse> workDayRules,
        List<CalendarExceptionResponse> exceptions,
        Instant createdAt,
        Instant updatedAt
) {}
