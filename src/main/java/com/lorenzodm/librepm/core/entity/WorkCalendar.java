package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a work calendar that defines working and non-working times.
 * Calendars can be applied to users, teams, or projects to accurately calculate capacity and schedule tasks.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "work_calendars")
public class WorkCalendar extends BaseSyncEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 32)
    private String scope = "WORKSPACE";

    @Column(name = "scope_entity_id", length = 36)
    private String scopeEntityId;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkDayRule> workDayRules = new ArrayList<>();

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalendarException> exceptions = new ArrayList<>();

    public WorkCalendar() {
        super();
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<WorkDayRule> getWorkDayRules() { return workDayRules; }
    public void setWorkDayRules(List<WorkDayRule> workDayRules) { this.workDayRules = workDayRules; }
    public List<CalendarException> getExceptions() { return exceptions; }
    public void setExceptions(List<CalendarException> exceptions) { this.exceptions = exceptions; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public String getScopeEntityId() { return scopeEntityId; }
    public void setScopeEntityId(String scopeEntityId) { this.scopeEntityId = scopeEntityId; }
}
