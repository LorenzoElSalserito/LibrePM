package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.SyncStatus;
import com.lorenzodm.librepm.core.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository per Task
 *
 * Query custom per:
 * - Ricerca per project/user
 * - Filtri multipli (status, priority, deadline)
 * - Ricerca full-text
 * - Ordinamenti custom
 * - Statistiche e analytics
 * - Sync operations (preparazione cloud)
 *
 * @author Lorenzo DM
 * @since 1.0.0
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    // ========================================
    // Query Base - Project
    // ========================================

    /**
     * Trova tutti i task di un progetto
     */
    List<Task> findByProjectId(String projectId);

    /**
     * Trova task attivi (non archiviati) di un progetto
     */
    List<Task> findByProjectIdAndArchivedFalse(String projectId);

    /**
     * Trova task archiviati di un progetto
     */
    List<Task> findByProjectIdAndArchivedTrue(String projectId);

    /**
     * Trova task specifico in un progetto (per ownership check)
     */
    Optional<Task> findByIdAndProjectId(String taskId, String projectId);

    /**
     * Verifica se task appartiene a progetto
     */
    boolean existsByIdAndProjectId(String taskId, String projectId);

    // ========================================
    // Query Filtri - Status (entity-based)
    // ========================================

    /**
     * Trova task per status name
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status.name = :statusName")
    List<Task> findByProjectIdAndStatusName(@Param("projectId") String projectId, @Param("statusName") String statusName);

    /**
     * Trova task attivi per status name
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status.name = :statusName AND t.archived = false")
    List<Task> findByProjectIdAndStatusNameAndArchivedFalse(@Param("projectId") String projectId, @Param("statusName") String statusName);

    /**
     * Trova task per status ID
     */
    List<Task> findByProjectIdAndStatusId(String projectId, String statusId);

    // ========================================
    // Query Filtri - Priority (entity-based)
    // ========================================

    /**
     * Trova task per priority name
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.priority.name = :priorityName")
    List<Task> findByProjectIdAndPriorityName(@Param("projectId") String projectId, @Param("priorityName") String priorityName);

    /**
     * Trova task attivi per priority name
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.priority.name = :priorityName AND t.archived = false")
    List<Task> findByProjectIdAndPriorityNameAndArchivedFalse(@Param("projectId") String projectId, @Param("priorityName") String priorityName);

    /**
     * Trova task per priority ID
     */
    List<Task> findByProjectIdAndPriorityId(String projectId, String priorityId);

    // ========================================
    // Query Filtri - Deadline
    // ========================================

    /**
     * Trova task con deadline entro una certa data
     */
    List<Task> findByProjectIdAndDeadlineBefore(String projectId, LocalDate deadline);

    /**
     * Trova task con deadline tra due date
     */
    List<Task> findByProjectIdAndDeadlineBetween(String projectId, LocalDate start, LocalDate end);

    /**
     * Trova task in ritardo (overdue)
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
            "AND t.deadline < :today " +
            "AND UPPER(t.status.name) NOT IN ('DONE', 'COMPLETED') " +
            "AND t.archived = false")
    List<Task> findOverdueTasks(@Param("projectId") String projectId, @Param("today") LocalDate today);

    /**
     * Trova task con deadline oggi
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
            "AND t.deadline = :today " +
            "AND t.archived = false")
    List<Task> findTasksDueToday(@Param("projectId") String projectId, @Param("today") LocalDate today);

    // ========================================
    // Query Filtri - Assegnazione
    // ========================================

    /**
     * Trova task assegnati a un user
     */
    List<Task> findByAssignedToId(String userId);

    /**
     * Trova task assegnati a un user in un progetto
     */
    List<Task> findByProjectIdAndAssignedToId(String projectId, String userId);

    /**
     * Trova task non assegnati di un progetto
     */
    List<Task> findByProjectIdAndAssignedToIsNull(String projectId);

    // ========================================
    // Query Ricerca - Full Text
    // ========================================

    /**
     * Ricerca task per titolo (case-insensitive, like)
     */
    List<Task> findByProjectIdAndTitleContainingIgnoreCase(String projectId, String searchText);

    /**
     * Ricerca task per titolo o descrizione
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
            "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    List<Task> searchByTitleOrDescription(
            @Param("projectId") String projectId,
            @Param("searchText") String searchText
    );

    /**
     * Ricerca avanzata con filtri multipli
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
            "AND (:statusName IS NULL OR t.status.name = :statusName) " +
            "AND (:priorityName IS NULL OR t.priority.name = :priorityName) " +
            "AND (:searchText IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
            "AND t.archived = false")
    List<Task> findWithFilters(
            @Param("projectId") String projectId,
            @Param("statusName") String statusName,
            @Param("priorityName") String priorityName,
            @Param("searchText") String searchText
    );

    // ========================================
    // Query Ordinamento
    // ========================================

    /**
     * Task ordinati per ultimo aggiornamento (desc)
     */
    List<Task> findByProjectIdOrderByUpdatedAtDesc(String projectId);

    /**
     * Task ordinati per creazione (desc)
     */
    List<Task> findByProjectIdOrderByCreatedAtDesc(String projectId);

    /**
     * Task ordinati per deadline (asc)
     */
    List<Task> findByProjectIdOrderByDeadlineAsc(String projectId);

    /**
     * Task ordinati per priorità (custom order: HIGH > MED > LOW)
     */
    @Query("SELECT t FROM Task t LEFT JOIN t.priority p WHERE t.project.id = :projectId " +
            "ORDER BY COALESCE(p.level, 999) ASC, t.updatedAt DESC")
    List<Task> findByProjectIdOrderByPriorityDesc(@Param("projectId") String projectId);

    /**
     * Task ordinati per sortOrder custom
     */
    List<Task> findByProjectIdOrderBySortOrderAsc(String projectId);

    // ========================================
    // Query Statistiche
    // ========================================

    /**
     * Conta task per status name
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status.name = :statusName")
    long countByProjectIdAndStatusName(@Param("projectId") String projectId, @Param("statusName") String statusName);

    /**
     * Conta task per priority name
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.priority.name = :priorityName")
    long countByProjectIdAndPriorityName(@Param("projectId") String projectId, @Param("priorityName") String priorityName);

    /**
     * Conta task in ritardo
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId " +
            "AND t.deadline < :today " +
            "AND t.status IS NOT NULL AND UPPER(t.status.name) NOT IN ('DONE', 'COMPLETED') " +
            "AND t.archived = false")
    long countOverdueTasks(@Param("projectId") String projectId, @Param("today") LocalDate today);

    /**
     * Conta task completati
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId " +
            "AND t.status IS NOT NULL AND UPPER(t.status.name) IN ('DONE', 'COMPLETED')")
    long countCompletedTasks(@Param("projectId") String projectId);

    /**
     * Percentuale completamento progetto
     */
    @Query("SELECT COUNT(CASE WHEN t.status IS NOT NULL AND UPPER(t.status.name) IN ('DONE', 'COMPLETED') THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0) " +
            "FROM Task t WHERE t.project.id = :projectId")
    Double getCompletionPercentage(@Param("projectId") String projectId);

    // ========================================
    // Query Asset
    // ========================================

    /**
     * Trova task con asset allegato
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.assetPath IS NOT NULL")
    List<Task> findTasksWithAssets(@Param("projectId") String projectId);

    /**
     * Conta task con asset
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.assetPath IS NOT NULL")
    long countTasksWithAssets(@Param("projectId") String projectId);

    // ========================================
    // Query Inbox (PRD-01-FR-004)
    // ========================================

    /**
     * Finds inbox tasks for a user (inbox=true, assigned to user)
     */
    @Query("SELECT t FROM Task t WHERE t.inbox = true AND t.assignedTo.id = :userId AND t.archived = false ORDER BY t.sortOrder ASC, t.createdAt DESC")
    List<Task> findInboxTasks(@Param("userId") String userId);

    /**
     * Counts inbox tasks for a user
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.inbox = true AND t.assignedTo.id = :userId AND t.archived = false")
    long countInboxTasks(@Param("userId") String userId);

    // ========================================
    // Query Sync (Cloud Ready)
    // ========================================

    /**
     * Trova task che necessitano sync
     */
    @Query("SELECT t FROM Task t WHERE t.syncStatus = :status OR t.lastSyncedAt < :threshold")
    List<Task> findNeedingSync(@Param("status") SyncStatus status, @Param("threshold") Instant threshold);

    /**
     * Trova task modificati dopo una certa data
     */
    List<Task> findByUpdatedAtAfter(Instant date);

    // ========================================
    // Query Focus Sessions
    // ========================================

    /**
     * Trova task con almeno una focus session
     */
    @Query("SELECT DISTINCT t FROM Task t JOIN t.focusSessions fs WHERE t.project.id = :projectId")
    List<Task> findTasksWithFocusSessions(@Param("projectId") String projectId);

    /**
     * Trova task più lavorati (ordinati per tempo focus totale)
     */
    @Query("SELECT t FROM Task t LEFT JOIN t.focusSessions fs " +
            "WHERE t.project.id = :projectId " +
            "GROUP BY t " +
            "ORDER BY SUM(COALESCE(fs.durationMs, 0)) DESC")
    List<Task> findMostFocusedTasks(@Param("projectId") String projectId);
}