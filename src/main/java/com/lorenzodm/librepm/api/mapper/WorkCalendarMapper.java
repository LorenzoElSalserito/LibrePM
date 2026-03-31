package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.CalendarExceptionResponse;
import com.lorenzodm.librepm.api.dto.response.WorkCalendarResponse;
import com.lorenzodm.librepm.api.dto.response.WorkDayRuleResponse;
import com.lorenzodm.librepm.core.entity.CalendarException;
import com.lorenzodm.librepm.core.entity.WorkCalendar;
import com.lorenzodm.librepm.core.entity.WorkDayRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WorkCalendarMapper {

    public WorkCalendarResponse toResponse(WorkCalendar calendar) {
        if (calendar == null) return null;
        List<WorkDayRuleResponse> rules = calendar.getWorkDayRules() != null
                ? calendar.getWorkDayRules().stream().map(this::toRuleResponse).collect(Collectors.toList())
                : List.of();
        List<CalendarExceptionResponse> exceptions = calendar.getExceptions() != null
                ? calendar.getExceptions().stream().map(this::toExceptionResponse).collect(Collectors.toList())
                : List.of();

        return new WorkCalendarResponse(
                calendar.getId(),
                calendar.getName(),
                calendar.getDescription(),
                rules,
                exceptions,
                calendar.getCreatedAt(),
                calendar.getUpdatedAt()
        );
    }

    public WorkDayRuleResponse toRuleResponse(WorkDayRule rule) {
        return new WorkDayRuleResponse(
                rule.getId(),
                rule.getCalendar().getId(),
                rule.getDayOfWeek(),
                rule.isWorkingDay(),
                rule.getStartTime(),
                rule.getEndTime(),
                rule.getBreakStartTime(),
                rule.getBreakEndTime()
        );
    }

    public CalendarExceptionResponse toExceptionResponse(CalendarException exception) {
        return new CalendarExceptionResponse(
                exception.getId(),
                exception.getCalendar().getId(),
                exception.getDate(),
                exception.isWorkingDay(),
                exception.getDescription()
        );
    }
}
