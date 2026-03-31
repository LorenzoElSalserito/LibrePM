package com.lorenzodm.librepm.api.dto.response;

import java.time.LocalDate;

public record CalendarExceptionResponse(
        String id,
        String calendarId,
        LocalDate date,
        boolean isWorkingDay,
        String description
) {}
