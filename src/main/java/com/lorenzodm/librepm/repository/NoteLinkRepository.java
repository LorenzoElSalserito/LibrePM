package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.NoteLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteLinkRepository extends JpaRepository<NoteLink, String> {

    List<NoteLink> findByNoteId(String noteId);

    @Query("SELECT nl FROM NoteLink nl WHERE nl.linkedEntityType = :entityType AND nl.linkedEntityId = :entityId AND nl.deletedAt IS NULL")
    List<NoteLink> findByLinkedEntity(
            @Param("entityType") NoteLink.LinkedEntityType entityType,
            @Param("entityId") String entityId);

    @Query("SELECT nl FROM NoteLink nl WHERE nl.note.id = :noteId AND nl.linkedEntityType = :entityType AND nl.deletedAt IS NULL")
    List<NoteLink> findByNoteIdAndType(@Param("noteId") String noteId, @Param("entityType") NoteLink.LinkedEntityType entityType);

    boolean existsByNoteIdAndLinkedEntityTypeAndLinkedEntityId(String noteId, NoteLink.LinkedEntityType entityType, String entityId);

    void deleteByNoteId(String noteId);

    @Query("SELECT COUNT(nl) FROM NoteLink nl WHERE nl.linkedEntityType = :entityType AND nl.linkedEntityId = :entityId AND nl.deletedAt IS NULL")
    long countByLinkedEntity(@Param("entityType") NoteLink.LinkedEntityType entityType, @Param("entityId") String entityId);
}
