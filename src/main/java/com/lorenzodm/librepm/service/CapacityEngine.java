package com.lorenzodm.librepm.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Capacity engine for work calendar-aware scheduling (PRD-09).
 * Calculates working minutes between dates based on a calendar.
 */
public interface CapacityEngine {

    /**
     * Returns the number of working minutes available in the calendar
     * for the given date range (inclusive).
     */
    int getWorkingMinutes(String calendarId, LocalDate from, LocalDate to);

    /**
     * Returns true if the given date is a working day per the calendar.
     */
    boolean isWorkingDay(String calendarId, LocalDate date);

    /**
     * Calculates the finish date/time by adding durationMinutes of working time
     * from a given start, using the specified calendar.
     */
    LocalDateTime addWorkingMinutes(String calendarId, LocalDateTime start, int durationMinutes);

    /**
     * Calculates the finish date/time from start using the default calendar.
     */
    LocalDateTime addWorkingMinutes(LocalDateTime start, int durationMinutes);

    /**
     * Returns working minutes in the period using the default calendar.
     */
    int getWorkingMinutes(LocalDate from, LocalDate to);
}
