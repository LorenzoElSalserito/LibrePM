package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.core.entity.CalendarException;
import com.lorenzodm.librepm.core.entity.WorkCalendar;
import com.lorenzodm.librepm.core.entity.WorkDayRule;
import com.lorenzodm.librepm.repository.CalendarExceptionRepository;
import com.lorenzodm.librepm.repository.WorkCalendarRepository;
import com.lorenzodm.librepm.repository.WorkDayRuleRepository;
import com.lorenzodm.librepm.service.CapacityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the capacity engine (PRD-09).
 * Calculates working time using work calendars, day rules, and exceptions.
 * Falls back to a standard 8h/day Mon-Fri schedule if no calendar data found.
 */
@Service
@Transactional(readOnly = true)
public class CapacityEngineImpl implements CapacityEngine {

    private static final Logger log = LoggerFactory.getLogger(CapacityEngineImpl.class);

    private static final String DEFAULT_CALENDAR_NAME = "Standard";
    private static final LocalTime DEFAULT_START = LocalTime.of(9, 0);
    private static final LocalTime DEFAULT_END = LocalTime.of(17, 0);
    private static final int DEFAULT_DAILY_MINUTES = 8 * 60; // 480 minutes

    private final WorkCalendarRepository workCalendarRepository;
    private final WorkDayRuleRepository workDayRuleRepository;
    private final CalendarExceptionRepository calendarExceptionRepository;

    public CapacityEngineImpl(WorkCalendarRepository workCalendarRepository,
                              WorkDayRuleRepository workDayRuleRepository,
                              CalendarExceptionRepository calendarExceptionRepository) {
        this.workCalendarRepository = workCalendarRepository;
        this.workDayRuleRepository = workDayRuleRepository;
        this.calendarExceptionRepository = calendarExceptionRepository;
    }

    @Override
    public int getWorkingMinutes(String calendarId, LocalDate from, LocalDate to) {
        Map<DayOfWeek, WorkDayRule> rules = loadRules(calendarId);
        Map<LocalDate, Boolean> exceptions = loadExceptions(calendarId, from, to);

        int total = 0;
        LocalDate current = from;
        while (!current.isAfter(to)) {
            WorkingSlot slot = getWorkingSlot(current, rules, exceptions);
            if (slot != null) {
                total += slot.getDurationMinutes();
            }
            current = current.plusDays(1);
        }
        return total;
    }

    @Override
    public boolean isWorkingDay(String calendarId, LocalDate date) {
        Map<DayOfWeek, WorkDayRule> rules = loadRules(calendarId);
        Map<LocalDate, Boolean> exceptions = loadExceptions(calendarId, date, date);
        return getWorkingSlot(date, rules, exceptions) != null;
    }

    @Override
    public LocalDateTime addWorkingMinutes(String calendarId, LocalDateTime start, int durationMinutes) {
        if (durationMinutes <= 0) return start;
        Map<DayOfWeek, WorkDayRule> rules = loadRules(calendarId);
        // We load exceptions progressively as needed, but for simplicity here we assume standard pattern
        // Ideally, we'd query exceptions in batches. For now, we query dynamically in the loop or assume small range.
        // Optimization: load exceptions for next 30 days?
        // Let's keep it simple: query per day inside helper, or accept query overhead.
        
        return advance(start, durationMinutes, rules, calendarId);
    }

    @Override
    public LocalDateTime addWorkingMinutes(LocalDateTime start, int durationMinutes) {
        Optional<WorkCalendar> defaultCal = workCalendarRepository.findByName(DEFAULT_CALENDAR_NAME);
        if (defaultCal.isEmpty()) {
            return advanceFallback(start, durationMinutes);
        }
        return addWorkingMinutes(defaultCal.get().getId(), start, durationMinutes);
    }

    @Override
    public int getWorkingMinutes(LocalDate from, LocalDate to) {
        Optional<WorkCalendar> defaultCal = workCalendarRepository.findByName(DEFAULT_CALENDAR_NAME);
        if (defaultCal.isEmpty()) {
            // Fallback: count Mon-Fri days * 480 minutes
            int total = 0;
            LocalDate current = from;
            while (!current.isAfter(to)) {
                DayOfWeek dow = current.getDayOfWeek();
                if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                    total += DEFAULT_DAILY_MINUTES;
                }
                current = current.plusDays(1);
            }
            return total;
        }
        return getWorkingMinutes(defaultCal.get().getId(), from, to);
    }

    // --- Private helpers ---

    private static class WorkingSlot {
        final LocalTime start;
        final LocalTime end;
        final LocalTime breakStart;
        final LocalTime breakEnd;

        WorkingSlot(LocalTime start, LocalTime end, LocalTime breakStart, LocalTime breakEnd) {
            this.start = start;
            this.end = end;
            this.breakStart = breakStart;
            this.breakEnd = breakEnd;
        }

        int getDurationMinutes() {
            int total = (int) ChronoUnit.MINUTES.between(start, end);
            if (breakStart != null && breakEnd != null) {
                total -= (int) ChronoUnit.MINUTES.between(breakStart, breakEnd);
            }
            return Math.max(0, total);
        }
    }

    private LocalDateTime advance(LocalDateTime current, int remainingMinutes,
                                  Map<DayOfWeek, WorkDayRule> rules, String calendarId) {
        LocalDateTime pointer = current;
        
        // Safety break to prevent infinite loops
        int daysProcessed = 0;
        while (remainingMinutes > 0 && daysProcessed < 3650) { // Max 10 years lookahead
            LocalDate date = pointer.toLocalDate();
            // Load exceptions for just this day (inefficient for long ranges, but correct)
            Map<LocalDate, Boolean> exceptions = loadExceptions(calendarId, date, date);
            
            WorkingSlot slot = getWorkingSlot(date, rules, exceptions);

            if (slot == null) {
                // Non-working day, jump to next day start (00:00) to re-evaluate
                pointer = date.plusDays(1).atStartOfDay();
                daysProcessed++;
                continue;
            }

            // Adjust pointer if before start of day
            if (pointer.toLocalTime().isBefore(slot.start)) {
                pointer = date.atTime(slot.start);
            }

            // If pointer is after end of day, move to next
            if (!pointer.toLocalTime().isBefore(slot.end)) {
                 pointer = date.plusDays(1).atStartOfDay();
                 daysProcessed++;
                 continue;
            }

            // If inside break, move to break end
            if (slot.breakStart != null && slot.breakEnd != null) {
                LocalTime t = pointer.toLocalTime();
                if (!t.isBefore(slot.breakStart) && t.isBefore(slot.breakEnd)) {
                    pointer = date.atTime(slot.breakEnd);
                }
            }

            // Calculate available minutes today from pointer
            LocalTime t = pointer.toLocalTime();
            int availableToday = 0;

            if (slot.breakStart != null && t.isBefore(slot.breakStart)) {
                // Before break: time to break + time after break
                int toBreak = (int) ChronoUnit.MINUTES.between(t, slot.breakStart);
                if (remainingMinutes <= toBreak) {
                    return pointer.plusMinutes(remainingMinutes);
                }
                remainingMinutes -= toBreak;
                pointer = date.atTime(slot.breakEnd); // Jump over break
                t = slot.breakEnd;
            }
            
            // Now we are either after break or there is no break
            // Time to end of day
            int toEnd = (int) ChronoUnit.MINUTES.between(t, slot.end);
            if (remainingMinutes <= toEnd) {
                return pointer.plusMinutes(remainingMinutes);
            }
            
            remainingMinutes -= toEnd;
            pointer = date.plusDays(1).atStartOfDay();
            daysProcessed++;
        }
        
        return pointer;
    }

    private LocalDateTime advanceFallback(LocalDateTime current, int remainingMinutes) {
        LocalDateTime pointer = current;
        int daysProcessed = 0;
        
        while (remainingMinutes > 0 && daysProcessed < 3650) {
            LocalDate date = pointer.toLocalDate();
            DayOfWeek dow = date.getDayOfWeek();
            
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                pointer = date.plusDays(1).atStartOfDay();
                daysProcessed++;
                continue;
            }

            // Mon-Fri 9-17
            if (pointer.toLocalTime().isBefore(DEFAULT_START)) {
                pointer = date.atTime(DEFAULT_START);
            }
            if (!pointer.toLocalTime().isBefore(DEFAULT_END)) {
                pointer = date.plusDays(1).atStartOfDay();
                daysProcessed++;
                continue;
            }

            int toEnd = (int) ChronoUnit.MINUTES.between(pointer.toLocalTime(), DEFAULT_END);
            if (remainingMinutes <= toEnd) {
                return pointer.plusMinutes(remainingMinutes);
            }
            remainingMinutes -= toEnd;
            pointer = date.plusDays(1).atStartOfDay();
            daysProcessed++;
        }
        return pointer;
    }

    private WorkingSlot getWorkingSlot(LocalDate date, Map<DayOfWeek, WorkDayRule> rules, Map<LocalDate, Boolean> exceptions) {
        // 1. Check exceptions
        if (exceptions.containsKey(date)) {
            Boolean isWorking = exceptions.get(date);
            if (Boolean.TRUE.equals(isWorking)) {
                // Exception says "Working Day". If no hours specified in exception entity (implied TODO), use default.
                // Current entity CalendarException only has boolean isWorkingDay.
                // PRD-09 implies exceptions could override hours. For now assume default hours if exception is true.
                return new WorkingSlot(DEFAULT_START, DEFAULT_END, null, null); 
            } else {
                return null; // Holiday
            }
        }

        // 2. Check rules
        WorkDayRule rule = rules.get(date.getDayOfWeek());
        if (rule == null) {
            // Default: Mon-Fri working, Sat-Sun off
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                 return new WorkingSlot(DEFAULT_START, DEFAULT_END, null, null);
            }
            return null;
        }

        if (!rule.isWorkingDay()) return null;

        LocalTime s = rule.getStartTime() != null ? rule.getStartTime() : DEFAULT_START;
        LocalTime e = rule.getEndTime() != null ? rule.getEndTime() : DEFAULT_END;
        return new WorkingSlot(s, e, rule.getBreakStartTime(), rule.getBreakEndTime());
    }

    private Map<DayOfWeek, WorkDayRule> loadRules(String calendarId) {
        List<WorkDayRule> list = workDayRuleRepository.findByCalendarId(calendarId);
        return list.stream().collect(Collectors.toMap(WorkDayRule::getDayOfWeek, r -> r));
    }

    private Map<LocalDate, Boolean> loadExceptions(String calendarId, LocalDate from, LocalDate to) {
        List<CalendarException> list = calendarExceptionRepository.findByCalendarIdAndDateBetween(calendarId, from, to);
        return list.stream().collect(Collectors.toMap(CalendarException::getDate, CalendarException::isWorkingDay));
    }
}
