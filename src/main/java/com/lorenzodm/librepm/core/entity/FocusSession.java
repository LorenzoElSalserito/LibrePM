package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity FocusSession - Rappresenta una sessione di focus/lavoro su un task
 *
 * Features:
 * - Tracking start/end timestamp
 * - Durata calcolata
 * - Relazione con Task
 * - Note opzionali per la sessione
 * - Cloud-sync ready
 *
 * Usato per:
 * - Statistiche produttività
 * - Time tracking
 * - Report giornalieri/settimanali
 *
 * @author Lorenzo DM
 * @since 1.0.0
 */
@Entity
@Table(name = "focus_sessions", indexes = {
        @Index(name = "idx_focus_task", columnList = "task_id"),
        @Index(name = "idx_focus_user", columnList = "user_id"),
        @Index(name = "idx_focus_started", columnList = "started_at"),
        @Index(name = "idx_focus_ended", columnList = "ended_at")
})
public class FocusSession {

    @Id
    @Column(length = 36)
    private String id;

    @NotNull(message = "Start timestamp obbligatorio")
    @Column(nullable = false)
    private Instant startedAt;

    @Column
    private Instant endedAt;

    @Column(nullable = false)
    private long durationMs = 0; // Durata in millisecondi

    @Column(length = 1000)
    private String notes; // Note sulla sessione (opzionale)

    @Column(length = 50)
    private String sessionType = "FOCUS"; // FOCUS, BREAK, DEEP_WORK, etc. (futuro)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Campi sync cloud (futuro)
    @Column
    private Instant lastSyncedAt;

    @Column(length = 20)
    private String syncStatus; // LOCAL_ONLY, SYNCED, CONFLICT, PENDING

    // Relazioni
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Costruttori
    public FocusSession() {
        this.id = UUID.randomUUID().toString();
        this.syncStatus = "LOCAL_ONLY";
        this.startedAt = Instant.now();
    }

    public FocusSession(Task task, User user) {
        this();
        this.task = task;
        this.user = user;
    }

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Helper methods

    /**
     * Termina la sessione focus (imposta endedAt e calcola durata)
     */
    public void endSession() {
        if (this.endedAt != null) {
            throw new IllegalStateException("Sessione già terminata");
        }

        this.endedAt = Instant.now();
        this.durationMs = Duration.between(startedAt, endedAt).toMillis();
    }

    /**
     * Verifica se la sessione è ancora in corso
     */
    public boolean isRunning() {
        return endedAt == null;
    }

    /**
     * Calcola la durata corrente (anche se sessione in corso)
     */
    public long getCurrentDurationMs() {
        if (isRunning()) {
            return Duration.between(startedAt, Instant.now()).toMillis();
        }
        return durationMs;
    }

    /**
     * Formatta durata in formato leggibile (HH:MM:SS)
     */
    public String getFormattedDuration() {
        long totalSeconds = durationMs / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, minutes, seconds);
        }
        return String.format("%02dm %02ds", minutes, seconds);
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FocusSession that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FocusSession{" +
                "id='" + id + '\'' +
                ", startedAt=" + startedAt +
                ", endedAt=" + endedAt +
                ", durationMs=" + durationMs +
                ", running=" + isRunning() +
                '}';
    }
}