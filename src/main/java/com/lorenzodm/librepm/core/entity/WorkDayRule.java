package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Defines the working hours for a specific day of the week within a calendar.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "work_day_rules", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"calendar_id", "dayOfWeek"})
})
public class WorkDayRule extends BaseSyncEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "calendar_id", nullable = false)
    private WorkCalendar calendar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private boolean isWorkingDay;

    @Column
    @Convert(converter = LocalTimeStringConverter.class)
    private LocalTime startTime;

    @Column
    @Convert(converter = LocalTimeStringConverter.class)
    private LocalTime endTime;

    @Column
    @Convert(converter = LocalTimeStringConverter.class)
    private LocalTime breakStartTime;

    @Column
    @Convert(converter = LocalTimeStringConverter.class)
    private LocalTime breakEndTime;

    public WorkDayRule() {
        super();
    }

    // Getters and Setters
    public WorkCalendar getCalendar() { return calendar; }
    public void setCalendar(WorkCalendar calendar) { this.calendar = calendar; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public boolean isWorkingDay() { return isWorkingDay; }
    public void setWorkingDay(boolean workingDay) { isWorkingDay = workingDay; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public LocalTime getBreakStartTime() { return breakStartTime; }
    public void setBreakStartTime(LocalTime breakStartTime) { this.breakStartTime = breakStartTime; }
    public LocalTime getBreakEndTime() { return breakEndTime; }
    public void setBreakEndTime(LocalTime breakEndTime) { this.breakEndTime = breakEndTime; }
}
