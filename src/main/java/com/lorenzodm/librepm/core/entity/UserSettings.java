package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity UserSettings - Impostazioni e preferenze utente
 *
 * Features:
 * - Tema (dark/light)
 * - Lingua (it/en)
 * - Notifiche abilitate
 * - Timer focus default
 * - Auto-backup
 * - Calendar Token (PRD-15)
 * - Daily Work Capacity (PRD-13)
 *
 * @author Lorenzo DM
 * @since 0.2.0
 * @updated 0.5.0 - Aggiunto calendarToken
 * @updated 0.5.3 - Aggiunto dailyWorkCapacityMinutes
 */
@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @Column(name = "user_id", length = 36)
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 20)
    private String theme = "dark"; // dark, light

    @Column(nullable = false, length = 10)
    private String language = "it"; // it, en

    @Column(nullable = false)
    private boolean notificationsEnabled = true;

    @Column(nullable = false)
    private int focusTimerDefaultMinutes = 25;

    @Column(nullable = false)
    private boolean autoBackupEnabled = true;

    @Column(nullable = false)
    private int dailyWorkCapacityMinutes = 480; // Default 8 hours (480 min)

    @Column
    private Instant lastBackupAt;

    @Column(length = 64)
    private String calendarToken; // Token segreto per iCal feed

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    // Costruttori
    public UserSettings() {
    }

    public UserSettings(User user) {
        this.user = user;
        this.userId = user.getId();
        this.calendarToken = UUID.randomUUID().toString(); // Genera token default
    }

    // Getters & Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public int getFocusTimerDefaultMinutes() {
        return focusTimerDefaultMinutes;
    }

    public void setFocusTimerDefaultMinutes(int focusTimerDefaultMinutes) {
        this.focusTimerDefaultMinutes = focusTimerDefaultMinutes;
    }

    public boolean isAutoBackupEnabled() {
        return autoBackupEnabled;
    }

    public void setAutoBackupEnabled(boolean autoBackupEnabled) {
        this.autoBackupEnabled = autoBackupEnabled;
    }

    public int getDailyWorkCapacityMinutes() {
        return dailyWorkCapacityMinutes;
    }

    public void setDailyWorkCapacityMinutes(int dailyWorkCapacityMinutes) {
        this.dailyWorkCapacityMinutes = dailyWorkCapacityMinutes;
    }

    public Instant getLastBackupAt() {
        return lastBackupAt;
    }

    public void setLastBackupAt(Instant lastBackupAt) {
        this.lastBackupAt = lastBackupAt;
    }

    public String getCalendarToken() {
        return calendarToken;
    }

    public void setCalendarToken(String calendarToken) {
        this.calendarToken = calendarToken;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserSettings that)) return false;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "userId='" + userId + '\'' +
                ", theme='" + theme + '\'' +
                ", language='" + language + '\'' +
                ", notificationsEnabled=" + notificationsEnabled +
                ", focusTimerDefaultMinutes=" + focusTimerDefaultMinutes +
                ", dailyWorkCapacityMinutes=" + dailyWorkCapacityMinutes +
                '}';
    }
}
