package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per entity Tag
 * 
 * @author Lorenzo DM
 * @since 0.2.0
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, String> {

    /**
     * Trova tag per nome (case-insensitive)
     */
    Optional<Tag> findByNameIgnoreCase(String name);

    /**
     * Trova tutti i tag di un utente
     */
    List<Tag> findByOwnerId(String ownerId);

    /**
     * Trova tutti i tag di un utente ordinati per nome
     */
    List<Tag> findByOwnerIdOrderByNameAsc(String ownerId);

    /**
     * Verifica se esiste un tag con quel nome per quell'utente
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM Tag t WHERE LOWER(t.name) = LOWER(:name) AND t.owner.id = :ownerId")
    boolean existsByNameAndOwnerId(@Param("name") String name, @Param("ownerId") String ownerId);

    /**
     * Cerca tag per nome parziale
     */
    @Query("SELECT t FROM Tag t WHERE t.owner.id = :ownerId " +
            "AND LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Tag> searchByName(@Param("ownerId") String ownerId, @Param("searchTerm") String searchTerm);

    /**
     * Conta quanti task usano questo tag
     */
    @Query("SELECT COUNT(t) FROM Task t JOIN t.tags tag WHERE tag.id = :tagId")
    long countTasksByTagId(@Param("tagId") String tagId);

    /**
     * Trova tag non usati da nessun task
     */
    @Query("SELECT t FROM Tag t WHERE t.owner.id = :ownerId AND SIZE(t.tasks) = 0")
    List<Tag> findUnusedTags(@Param("ownerId") String ownerId);

    /**
     * Trova tag più usati (top N)
     */
    @Query("SELECT t FROM Tag t WHERE t.owner.id = :ownerId " +
            "ORDER BY SIZE(t.tasks) DESC")
    List<Tag> findMostUsedTags(@Param("ownerId") String ownerId);
}
