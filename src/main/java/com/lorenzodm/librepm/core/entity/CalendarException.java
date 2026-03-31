package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Represents an exception to the standard work calendar, such as a holiday or a special working day.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "calendar_exceptions", indexes = {
    @Index(name = "idx_exception_calendar", columnList = "calendar_id"),
    @Index(name = "idx_exception_date", columnList = "exception_date")
})
public class CalendarException extends BaseSyncEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "calendar_id", nullable = false)
    private WorkCalendar calendar;

    @Column(name = "exception_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private boolean isWorkingDay;

    @Column(length = 255)
    private String description;

    public CalendarException() {
        super();
    }

    // Getters and Setters
    public WorkCalendar getCalendar() { return calendar; }
    public void setCalendar(WorkCalendar calendar) { this.calendar = calendar; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public boolean isWorkingDay() { return isWorkingDay; }
    public void setWorkingDay(boolean workingDay) { isWorkingDay = workingDay; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
