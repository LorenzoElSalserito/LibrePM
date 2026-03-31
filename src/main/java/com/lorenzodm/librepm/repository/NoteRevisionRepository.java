package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.NoteRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRevisionRepository extends JpaRepository<NoteRevision, String> {

    List<NoteRevision> findByNoteIdOrderByRevisionNumberDesc(String noteId);

    @Query("SELECT MAX(r.revisionNumber) FROM NoteRevision r WHERE r.noteId = :noteId")
    Optional<Integer> findMaxRevisionNumber(@Param("noteId") String noteId);
}
