package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.CreateNoteLinkRequest;
import com.lorenzodm.librepm.core.entity.NoteLink;

import java.util.List;

public interface NoteLinkService {
    NoteLink create(CreateNoteLinkRequest request);
    List<NoteLink> findByNoteId(String noteId);
    List<NoteLink> findByLinkedEntity(String entityType, String entityId);
    void delete(String id);
}
