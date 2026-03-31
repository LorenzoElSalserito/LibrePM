package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lorenzodm.librepm.core.entity.SyncStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository per Project
 *
 * Query custom per:
 * - Ricerca per owner
 * - Filtro progetti archiviati/favoriti
 * - Statistiche completamento
 * - Sync operations (preparazione cloud)
 *
 * @author Lorenzo DM
 * @since 1.0.0
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    /**
     * Trova progetto per nome e owner (used for inbox project lookup)
     */
    Optional<Project> findByNameAndOwnerId(String name, String ownerId);

    /**
     * Trova tutti i progetti di un owner
     */
    List<Project> findByOwnerId(String ownerId);

    /**
     * Trova progetti attivi (non archiviati) di un owner
     */
    List<Project> findByOwnerIdAndArchivedFalse(String ownerId);

    /**
     * Trova progetti favoriti di un owner
     */
    List<Project> findByOwnerIdAndFavoriteTrue(String ownerId);

    /**
     * Trova progetti archiviati di un owner
     */
    List<Project> findByOwnerIdAndArchivedTrue(String ownerId);

    /**
     * Trova progetti per nome (case-insensitive, like)
     */
    List<Project> findByNameContainingIgnoreCase(String name);

    /**
     * Trova progetti di un owner per nome
     */
    List<Project> findByOwnerIdAndNameContainingIgnoreCase(String ownerId, String name);

    /**
     * Trova progetti creati dopo una certa data
     */
    List<Project> findByCreatedAtAfter(Instant date);

    /**
     * Trova progetti aggiornati dopo una certa data
     */
    List<Project> findByUpdatedAtAfter(Instant date);

    /**
     * Trova progetto specifico di un owner (per sicurezza ownership)
     */
    Optional<Project> findByIdAndOwnerId(String projectId, String ownerId);

    /**
     * Verifica se progetto appartiene a owner
     */
    boolean existsByIdAndOwnerId(String projectId, String ownerId);

    /**
     * Conta progetti attivi di un owner
     */
    long countByOwnerIdAndArchivedFalse(String ownerId);

    /**
     * Trova progetti con percentuale completamento > threshold
     */
    @Query("SELECT p FROM Project p WHERE p.owner.id = :ownerId " +
            "AND (SELECT COUNT(t) * 100.0 / NULLIF(SIZE(p.tasks), 0) " +
            "FROM Task t WHERE t.project = p AND UPPER(t.status.name) IN ('DONE', 'COMPLETED')) >= :minPercentage")
    List<Project> findByOwnerWithCompletionAbove(
            @Param("ownerId") String ownerId,
            @Param("minPercentage") double minPercentage
    );

    /**
     * Trova progetti che necessitano sync (cloud ready)
     */
    @Query("SELECT p FROM Project p WHERE p.syncStatus = :status OR p.lastSyncedAt < :threshold")
    List<Project> findNeedingSync(@Param("status") SyncStatus status, @Param("threshold") Instant threshold);

    /**
     * Trova progetti ordinati per aggiornamento recente
     */
    List<Project> findByOwnerIdOrderByUpdatedAtDesc(String ownerId);

    /**
     * Trova progetti ordinati per creazione
     */
    List<Project> findByOwnerIdOrderByCreatedAtDesc(String ownerId);

    /**
     * Statistiche: conta totale progetti per owner
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.owner.id = :ownerId")
    long countTotalByOwner(@Param("ownerId") String ownerId);

    /**
     * Statistiche: conta progetti favoriti per owner
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.owner.id = :ownerId AND p.favorite = true")
    long countFavoritesByOwner(@Param("ownerId") String ownerId);

    /**
     * Trova tutti i progetti di cui l'utente è membro (incluso owner)
     */
    @Query("SELECT DISTINCT p FROM Project p JOIN p.members m WHERE m.user.id = :userId")
    List<Project> findAllByMemberId(@Param("userId") String userId);

    /**
     * Trova progetti attivi di cui l'utente è membro
     */
    @Query("SELECT DISTINCT p FROM Project p JOIN p.members m WHERE m.user.id = :userId AND p.archived = false")
    List<Project> findAllActiveByMemberId(@Param("userId") String userId);

    /**
     * Trova progetti archiviati di cui l'utente è membro
     */
    @Query("SELECT DISTINCT p FROM Project p JOIN p.members m WHERE m.user.id = :userId AND p.archived = true")
    List<Project> findAllArchivedByMemberId(@Param("userId") String userId);
}
