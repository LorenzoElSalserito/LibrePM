package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateNoteLinkRequest;
import com.lorenzodm.librepm.api.exception.ConflictException;
import com.lorenzodm.librepm.api.exception.OwnershipViolationException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.Note;
import com.lorenzodm.librepm.core.entity.NoteLink;
import com.lorenzodm.librepm.repository.NoteLinkRepository;
import com.lorenzodm.librepm.repository.NoteRepository;
import com.lorenzodm.librepm.repository.ProjectMemberRepository;
import com.lorenzodm.librepm.service.NoteLinkService;
import com.lorenzodm.librepm.service.NoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class NoteLinkServiceImpl implements NoteLinkService {

    private static final Logger log = LoggerFactory.getLogger(NoteLinkServiceImpl.class);
    private final NoteLinkRepository noteLinkRepository;
    private final NoteRepository noteRepository;
    private final NoteService noteService;
    private final ProjectMemberRepository projectMemberRepository;

    public NoteLinkServiceImpl(NoteLinkRepository noteLinkRepository, NoteRepository noteRepository,
                               NoteService noteService, ProjectMemberRepository projectMemberRepository) {
        this.noteLinkRepository = noteLinkRepository;
        this.noteRepository = noteRepository;
        this.noteService = noteService;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Override
    public NoteLink create(String userId, CreateNoteLinkRequest request) {
        log.debug("Creazione link nota: note={}, type={}, entity={}", 
                request.noteId(), request.linkedEntityType(), request.linkedEntityId());

        // ANTI-IDOR: accesso al contesto, poi solo l'autore collega la propria nota
        Note note = noteService.get(userId, request.noteId());
        requireOwner(userId, note);

        NoteLink.LinkedEntityType entityType;
        try {
            entityType = NoteLink.LinkedEntityType.valueOf(request.linkedEntityType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo entità non valido: " + request.linkedEntityType());
        }

        if (noteLinkRepository.existsByNoteIdAndLinkedEntityTypeAndLinkedEntityId(
                request.noteId(), entityType, request.linkedEntityId())) {
            throw new ConflictException("Link già esistente");
        }

        NoteLink link = new NoteLink();
        link.setNote(note);
        link.setLinkedEntityType(entityType);
        link.setLinkedEntityId(request.linkedEntityId());
        return noteLinkRepository.save(link);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteLink> findByNoteId(String userId, String noteId) {
        noteService.get(userId, noteId);
        return noteLinkRepository.findByNoteId(noteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteLink> findByLinkedEntity(String userId, String entityType, String entityId) {
        NoteLink.LinkedEntityType type = NoteLink.LinkedEntityType.valueOf(entityType.toUpperCase());
        Set<String> projectIds = projectMemberRepository.findByUserId(userId).stream()
                .map(pm -> pm.getProject().getId())
                .collect(Collectors.toSet());
        return noteLinkRepository.findByLinkedEntity(type, entityId).stream()
                .filter(link -> link.getNote().getDeletedAt() == null && projectIds.contains(link.getNote().getProjectId()))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String userId, String id) {
        log.debug("Eliminazione link nota: {}", id);
        NoteLink link = noteLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Link nota non trovato: " + id));
        requireOwner(userId, noteService.get(userId, link.getNote().getId()));
        noteLinkRepository.delete(link);
    }

    private void requireOwner(String userId, Note note) {
        if (!note.getOwner().getId().equals(userId)) {
            throw new OwnershipViolationException("Non puoi modificare i collegamenti di una nota di un altro utente");
        }
    }
}
