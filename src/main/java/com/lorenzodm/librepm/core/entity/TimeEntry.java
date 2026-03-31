package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Represents a consolidated time log entry for a task.
 * This entity is designed to store both automated (from Focus Sessions) and manual time entries,
 * providing a unified source for time tracking data as per PRD-03.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "time_entries", indexes = {
    @Index(name = "idx_timeentry_task", columnList = "task_id"),
    @Index(name = "idx_timeentry_user", columnList = "user_id"),
    @Index(name = "idx_timeentry_date", columnList = "entry_date")
})
public class TimeEntry extends BaseSyncEntity {

    public enum EntryType {
        TIMER,
        MANUAL
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntryType type;

    @Column(length = 500)
    private String description;

    public TimeEntry() {
        super();
    }

    // Getters and Setters
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public EntryType getType() { return type; }
    public void setType(EntryType type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
