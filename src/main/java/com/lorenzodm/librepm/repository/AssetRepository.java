package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Asset;
import com.lorenzodm.librepm.core.entity.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Asset} entities.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 0.5.2
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, String> {

    // ========================================
    // Base Queries - Owner
    // ========================================

    /**
     * Finds all assets for a given owner, including soft-deleted ones.
     */
    List<Asset> findByOwnerId(String ownerId);

    /**
     * Finds all active (not deleted) assets for a given owner.
     */
    List<Asset> findByOwnerIdAndDeletedAtIsNull(String ownerId);

    /**
     * Finds all soft-deleted assets for a given owner.
     */
    List<Asset> findByOwnerIdAndDeletedAtIsNotNull(String ownerId);

    /**
     * Finds a specific asset by its ID and owner ID for ownership verification.
     */
    Optional<Asset> findByIdAndOwnerId(String assetId, String ownerId);

    /**
     * Checks if an asset belongs to a specific owner.
     */
    boolean existsByIdAndOwnerId(String assetId, String ownerId);

    // ========================================
    // Filter Queries - File Type
    // ========================================

    /**
     * Finds assets by MIME type for a given owner.
     */
    List<Asset> findByOwnerIdAndMimeType(String ownerId, String mimeType);

    /**
     * Finds all image assets for a given owner.
     */
    @Query("SELECT a FROM Asset a WHERE a.owner.id = :ownerId " +
            "AND a.mimeType LIKE 'image/%' " +
            "AND a.deletedAt IS NULL")
    List<Asset> findImagesByOwner(@Param("ownerId") String ownerId);

    /**
     * Finds all document assets for a given owner.
     */
    @Query("SELECT a FROM Asset a WHERE a.owner.id = :ownerId " +
            "AND (a.mimeType LIKE '%pdf%' OR a.mimeType LIKE '%document%' " +
            "OR a.mimeType LIKE '%word%' OR a.mimeType LIKE '%excel%' " +
            "OR a.mimeType LIKE '%powerpoint%') " +
            "AND a.deletedAt IS NULL")
    List<Asset> findDocumentsByOwner(@Param("ownerId") String ownerId);

    /**
     * Finds assets by file extension for a given owner.
     */
    @Query("SELECT a FROM Asset a WHERE a.owner.id = :ownerId " +
            "AND LOWER(a.fileName) LIKE LOWER(CONCAT('%', :extension)) " +
            "AND a.deletedAt IS NULL")
    List<Asset> findByOwnerAndExtension(
            @Param("ownerId") String ownerId,
            @Param("extension") String extension
    );

    // ========================================
    // Search Queries
    // ========================================

    /**
     * Searches for assets by file name (case-insensitive, partial match).
     */
    List<Asset> findByOwnerIdAndFileNameContainingIgnoreCase(String ownerId, String searchText);

    /**
     * Searches for assets by file name or description.
     */
    @Query("SELECT a FROM Asset a WHERE a.owner.id = :ownerId " +
            "AND (LOWER(a.fileName) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "OR LOWER(a.description) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
            "AND a.deletedAt IS NULL")
    List<Asset> searchByFileNameOrDescription(
            @Param("ownerId") String ownerId,
            @Param("searchText") String searchText
    );

    // ========================================
    // Storage Queries - Path & Checksum
    // ========================================

    /**
     * Finds an asset by its unique file path.
     */
    Optional<Asset> findByFilePath(String filePath);

    /**
     * Checks if a file path already exists.
     */
    boolean existsByFilePath(String filePath);

    /**
     * Finds assets by checksum (to detect duplicates).
     */
    List<Asset> findByChecksum(String checksum);

    /**
     * Checks if a checksum already exists.
     */
    boolean existsByChecksum(String checksum);

    /**
     * Finds duplicate assets by checksum (active only).
     */
    @Query("SELECT a FROM Asset a WHERE a.checksum = :checksum AND a.deletedAt IS NULL")
    List<Asset> findDuplicatesByChecksum(@Param("checksum") String checksum);

    // ========================================
    // Storage Queries - Size
    // ========================================

    /**
     * Finds assets larger than a specified size.
     */
    @Query("SELECT a FROM Asset a WHERE a.owner.id = :ownerId " +
            "AND a.sizeBytes > :minSize " +
            "AND a.deletedAt IS NULL")
    List<Asset> findLargerThan(@Param("ownerId") String ownerId, @Param("minSize") Long minSize);

    /**
     * Calculates the total storage space used by an owner (active assets only).
     */
    @Query("SELECT SUM(a.sizeBytes) FROM Asset a WHERE a.owner.id = :ownerId AND a.deletedAt IS NULL")
    Long getTotalStorageByOwner(@Param("ownerId") String ownerId);

    /**
     * Calculates the total system storage space used (all active assets).
     */
    @Query("SELECT SUM(a.sizeBytes) FROM Asset a WHERE a.deletedAt IS NULL")
    Long getTotalSystemStorage();

    // ========================================
    // Temporal Queries
    // ========================================

    /**
     * Finds assets created after a specific date.
     */
    List<Asset> findByCreatedAtAfter(Instant date);

    /**
     * Finds assets that have not been accessed since a given threshold (for cleanup).
     */
    @Query("SELECT a FROM Asset a WHERE a.lastAccessedAt < :threshold OR a.lastAccessedAt IS NULL")
    List<Asset> findNotAccessedSince(@Param("threshold") Instant threshold);

    /**
     * Finds assets ordered by creation date (oldest first).
     */
    List<Asset> findByOwnerIdOrderByCreatedAtAsc(String ownerId);

    /**
     * Finds assets ordered by creation date (newest first).
     */
    List<Asset> findByOwnerIdOrderByCreatedAtDesc(String ownerId);

    /**
     * Finds assets ordered by last access date.
     */
    List<Asset> findByOwnerIdOrderByLastAccessedAtDesc(String ownerId);

    // ========================================
    // Statistical Queries
    // ========================================

    /**
     * Counts all assets for an owner.
     */
    long countByOwnerId(String ownerId);

    /**
     * Counts active assets for an owner.
     */
    long countByOwnerIdAndDeletedAtIsNull(String ownerId);

    /**
     * Counts image assets for an owner.
     */
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.owner.id = :ownerId " +
            "AND a.mimeType LIKE 'image/%' " +
            "AND a.deletedAt IS NULL")
    long countImagesByOwner(@Param("ownerId") String ownerId);

    /**
     * Counts document assets for an owner.
     */
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.owner.id = :ownerId " +
            "AND (a.mimeType LIKE '%pdf%' OR a.mimeType LIKE '%document%') " +
            "AND a.deletedAt IS NULL")
    long countDocumentsByOwner(@Param("ownerId") String ownerId);

    /**
     * Counts assets grouped by MIME type for an owner.
     */
    @Query("SELECT a.mimeType, COUNT(a) FROM Asset a " +
            "WHERE a.owner.id = :ownerId AND a.deletedAt IS NULL " +
            "GROUP BY a.mimeType")
    List<Object[]> countByMimeType(@Param("ownerId") String ownerId);

    // ========================================
    // Thumbnail Queries
    // ========================================

    /**
     * Finds assets that have a generated thumbnail.
     */
    @Query("SELECT a FROM Asset a WHERE a.owner.id = :ownerId " +
            "AND a.thumbnailPath IS NOT NULL " +
            "AND a.deletedAt IS NULL")
    List<Asset> findWithThumbnail(@Param("ownerId") String ownerId);

    /**
     * Finds image assets that need a thumbnail.
     */
    @Query("SELECT a FROM Asset a WHERE a.owner.id = :ownerId " +
            "AND a.mimeType LIKE 'image/%' " +
            "AND a.thumbnailPath IS NULL " +
            "AND a.deletedAt IS NULL")
    List<Asset> findImagesWithoutThumbnail(@Param("ownerId") String ownerId);

    // ========================================
    // Sync Queries (Cloud Ready)
    // ========================================

    /**
     * Finds assets that need synchronization.
     */
    @Query("SELECT a FROM Asset a WHERE a.syncStatus = :status OR a.lastSyncedAt < :threshold")
    List<Asset> findNeedingSync(@Param("status") SyncStatus status, @Param("threshold") Instant threshold);

    /**
     * Finds assets that have been synced to the cloud.
     */
    @Query("SELECT a FROM Asset a WHERE a.cloudUrl IS NOT NULL AND a.deletedAt IS NULL")
    List<Asset> findSyncedToCloud();

    /**
     * Finds assets that exist only locally.
     */
    @Query("SELECT a FROM Asset a WHERE a.cloudUrl IS NULL AND a.syncStatus = 'LOCAL_ONLY' AND a.deletedAt IS NULL")
    List<Asset> findLocalOnly();

    // ========================================
    // Soft Delete & Cleanup Queries
    // ========================================

    /**
     * Finds soft-deleted assets older than a threshold for permanent cleanup.
     */
    @Query("SELECT a FROM Asset a WHERE a.deletedAt IS NOT NULL " +
            "AND a.deletedAt < :threshold")
    List<Asset> findDeletedForCleanup(@Param("threshold") Instant threshold);

    /**
     * Counts all soft-deleted assets.
     */
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.deletedAt IS NOT NULL")
    long countDeleted();

    /**
     * Calculates the total storage space that can be recovered from soft-deleted assets.
     */
    @Query("SELECT SUM(a.sizeBytes) FROM Asset a WHERE a.deletedAt IS NOT NULL")
    Long getRecoverableStorage();
}
