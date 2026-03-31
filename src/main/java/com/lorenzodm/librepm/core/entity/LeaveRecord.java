package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leave_records", uniqueConstraints =
    @UniqueConstraint(columnNames = {"user_id", "leave_date"}))
public class LeaveRecord {

    public enum LeaveType {
        VACATION, SICK, PERSONAL, TRAINING
    }

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "leave_date", nullable = false)
    private LocalDate leaveDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", length = 32)
    private LeaveType leaveType;

    @Column
    private Double hours;

    @Column(columnDefinition = "TEXT")
    private String description;

    public LeaveRecord() { this.id = UUID.randomUUID().toString(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getLeaveDate() { return leaveDate; }
    public void setLeaveDate(LocalDate leaveDate) { this.leaveDate = leaveDate; }
    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }
    public Double getHours() { return hours; }
    public void setHours(Double hours) { this.hours = hours; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
