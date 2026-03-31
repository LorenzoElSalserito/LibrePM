package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.FocusSession;
import com.lorenzodm.librepm.core.entity.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository per FocusSession
 *
 * Query custom per:
 * - Ricerca per task/user
 * - Filtri temporali (oggi, settimana, mese)
 * - Statistiche produttività
 * - Analytics tempo focus
 * - Sync operations (preparazione cloud)
 *
 * @author Lorenzo DM
 * @since 1.0.0
 */
@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, String> {

    // ========================================
    // Query Base - Task
    // ========================================

    /**
     * Trova tutte le sessioni di un task
     */
    List<FocusSession> findByTaskId(String taskId);

    /**
     * Trova sessioni di un task ordinate per data (desc)
     */
    List<FocusSession> findByTaskIdOrderByStartedAtDesc(String taskId);

    /**
     * Trova ultima sessione di un task
     */
    Optional<FocusSession> findFirstByTaskIdOrderByStartedAtDesc(String taskId);

    // ========================================
    // Query Base - User
    // ========================================

    /**
     * Trova tutte le sessioni di un user
     */
    List<FocusSession> findByUserId(String userId);

    /**
     * Trova sessioni di un user ordinate per data (desc)
     */
    List<FocusSession> findByUserIdOrderByStartedAtDesc(String userId);

    // ========================================
    // Query Filtri Temporali
    // ========================================

    /**
     * Trova sessioni di un task tra due date
     */
    List<FocusSession> findByTaskIdAndStartedAtBetween(String taskId, Instant start, Instant end);

    /**
     * Trova sessioni di un user tra due date
     */
    List<FocusSession> findByUserIdAndStartedAtBetween(String userId, Instant start, Instant end);

    /**
     * Trova sessioni dopo una certa data
     */
    List<FocusSession> findByStartedAtAfter(Instant date);

    /**
     * Trova sessioni di un user dopo una certa data
     */
    List<FocusSession> findByUserIdAndStartedAtAfter(String userId, Instant date);

    /**
     * Trova sessioni di un task dopo una certa data
     */
    List<FocusSession> findByTaskIdAndStartedAtAfter(String taskId, Instant date);

    /**
     * Trova sessioni di oggi per un user
     */
    @Query("SELECT fs FROM FocusSession fs WHERE fs.user.id = :userId " +
            "AND fs.startedAt >= :todayStart " +
            "AND fs.startedAt < :tomorrowStart")
    List<FocusSession> findTodaySessionsByUser(
            @Param("userId") String userId,
            @Param("todayStart") Instant todayStart,
            @Param("tomorrowStart") Instant tomorrowStart
    );

    /**
     * Trova sessioni di questa settimana per un user
     */
    @Query("SELECT fs FROM FocusSession fs WHERE fs.user.id = :userId " +
            "AND fs.startedAt >= :weekStart")
    List<FocusSession> findThisWeekSessionsByUser(
            @Param("userId") String userId,
            @Param("weekStart") Instant weekStart
    );

    /**
     * Trova sessioni di questo mese per un user
     */
    @Query("SELECT fs FROM FocusSession fs WHERE fs.user.id = :userId " +
            "AND fs.startedAt >= :monthStart")
    List<FocusSession> findThisMonthSessionsByUser(
            @Param("userId") String userId,
            @Param("monthStart") Instant monthStart
    );

    // ========================================
    // Query Sessioni Attive (Running)
    // ========================================

    /**
     * Trova sessioni attive (senza endedAt) di un user
     */
    @Query("SELECT fs FROM FocusSession fs WHERE fs.user.id = :userId AND fs.endedAt IS NULL")
    List<FocusSession> findActiveSessionsByUser(@Param("userId") String userId);

    /**
     * Trova sessione attiva (running) di un user - dovrebbe essere max 1
     */
    @Query("SELECT fs FROM FocusSession fs WHERE fs.user.id = :userId AND fs.endedAt IS NULL")
    Optional<FocusSession> findRunningSessionByUser(@Param("userId") String userId);

    /**
     * Verifica se user ha sessione attiva
     */
    @Query("SELECT COUNT(fs) > 0 FROM FocusSession fs WHERE fs.user.id = :userId AND fs.endedAt IS NULL")
    boolean hasActiveSession(@Param("userId") String userId);

    // ========================================
    // Query Statistiche - Tempo Focus
    // ========================================

    /**
     * Tempo totale focus su un task (millisecondi)
     */
    @Query("SELECT SUM(fs.durationMs) FROM FocusSession fs WHERE fs.task.id = :taskId")
    Long getTotalFocusTimeForTask(@Param("taskId") String taskId);

    /**
     * Tempo totale focus di un user (millisecondi)
     */
    @Query("SELECT SUM(fs.durationMs) FROM FocusSession fs WHERE fs.user.id = :userId")
    Long getTotalFocusTimeForUser(@Param("userId") String userId);

    /**
     * Tempo focus di oggi per un user
     */
    @Query("SELECT SUM(fs.durationMs) FROM FocusSession fs WHERE fs.user.id = :userId " +
            "AND fs.startedAt >= :todayStart " +
            "AND fs.startedAt < :tomorrowStart")
    Long getTodayFocusTimeForUser(
            @Param("userId") String userId,
            @Param("todayStart") Instant todayStart,
            @Param("tomorrowStart") Instant tomorrowStart
    );

    /**
     * Tempo focus di questa settimana per un user
     */
    @Query("SELECT SUM(fs.durationMs) FROM FocusSession fs WHERE fs.user.id = :userId " +
            "AND fs.startedAt >= :weekStart")
    Long getThisWeekFocusTimeForUser(
            @Param("userId") String userId,
            @Param("weekStart") Instant weekStart
    );

    /**
     * Tempo focus di questo mese per un user
     */
    @Query("SELECT SUM(fs.durationMs) FROM FocusSession fs WHERE fs.user.id = :userId " +
            "AND fs.startedAt >= :monthStart")
    Long getThisMonthFocusTimeForUser(
            @Param("userId") String userId,
            @Param("monthStart") Instant monthStart
    );

    /**
     * Media durata sessioni per un task
     */
    @Query("SELECT AVG(fs.durationMs) FROM FocusSession fs WHERE fs.task.id = :taskId")
    Double getAverageFocusTimeForTask(@Param("taskId") String taskId);

    // ========================================
    // Query Statistiche - Conteggi
    // ========================================

    /**
     * Conta sessioni di un task
     */
    long countByTaskId(String taskId);

    /**
     * Conta sessioni di un user
     */
    long countByUserId(String userId);

    /**
     * Conta sessioni di oggi per un user
     */
    @Query("SELECT COUNT(fs) FROM FocusSession fs WHERE fs.user.id = :userId " +
            "AND fs.startedAt >= :todayStart " +
            "AND fs.startedAt < :tomorrowStart")
    long countTodaySessionsByUser(
            @Param("userId") String userId,
            @Param("todayStart") Instant todayStart,
            @Param("tomorrowStart") Instant tomorrowStart
    );

    /**
     * Conta sessioni di questa settimana per un user
     */
    @Query("SELECT COUNT(fs) FROM FocusSession fs WHERE fs.user.id = :userId " +
            "AND fs.startedAt >= :weekStart")
    long countThisWeekSessionsByUser(
            @Param("userId") String userId,
            @Param("weekStart") Instant weekStart
    );

    // ========================================
    // Query Analytics - Top Tasks
    // ========================================

    /**
     * Trova i 10 task più lavorati di un user
     */
    @Query("SELECT fs.task.id, SUM(fs.durationMs) as totalTime " +
            "FROM FocusSession fs " +
            "WHERE fs.user.id = :userId " +
            "GROUP BY fs.task.id " +
            "ORDER BY totalTime DESC")
    List<Object[]> findTopTasksByFocusTime(@Param("userId") String userId);

    /**
     * Trova task lavorati oggi da un user
     */
    @Query("SELECT DISTINCT fs.task FROM FocusSession fs WHERE fs.user.id = :userId " +
            "AND fs.startedAt >= :todayStart " +
            "AND fs.startedAt < :tomorrowStart")
    List<Object> findTasksWorkedTodayByUser(
            @Param("userId") String userId,
            @Param("todayStart") Instant todayStart,
            @Param("tomorrowStart") Instant tomorrowStart
    );

    // ========================================
    // Query Analytics - Timeline
    // ========================================

    /**
     * Trova sessioni per giorno (per grafici timeline)
     */
    @Query("SELECT CAST(fs.startedAt AS date), COUNT(fs), SUM(fs.durationMs) " +
            "FROM FocusSession fs " +
            "WHERE fs.user.id = :userId " +
            "AND fs.startedAt BETWEEN :start AND :end " +
            "GROUP BY CAST(fs.startedAt AS date) " +
            "ORDER BY CAST(fs.startedAt AS date)")
    List<Object[]> findDailyStatsByUser(
            @Param("userId") String userId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    // ========================================
    // Query Sync (Cloud Ready)
    // ========================================

    /**
     * Trova sessioni che necessitano sync
     */
    @Query("SELECT fs FROM FocusSession fs WHERE fs.syncStatus = :status OR fs.lastSyncedAt < :threshold")
    List<FocusSession> findNeedingSync(@Param("status") SyncStatus status, @Param("threshold") Instant threshold);

    /**
     * Trova sessioni create dopo una certa data
     */
    List<FocusSession> findByCreatedAtAfter(Instant date);

    // ========================================
    // Query Tipo Sessione (Futuro)
    // ========================================

    /**
     * Trova sessioni per tipo
     */
    List<FocusSession> findByUserIdAndSessionType(String userId, String sessionType);

    /**
     * Conta sessioni per tipo
     */
    long countByUserIdAndSessionType(String userId, String sessionType);
}
