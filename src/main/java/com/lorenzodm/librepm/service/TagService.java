package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.CreateTagRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTagRequest;
import com.lorenzodm.librepm.core.entity.Tag;

import java.util.List;

/**
 * Service per gestione Tag
 *
 * @author Lorenzo DM
 * @since 0.2.0
 */
public interface TagService {

    /**
     * Crea nuovo tag per utente
     */
    Tag create(String userId, CreateTagRequest request);

    /**
     * Ottieni tag per ID (verifica ownership)
     */
    Tag getOwned(String userId, String tagId);

    /**
     * Lista tutti i tag dell'utente
     */
    List<Tag> listOwned(String userId);

    /**
     * Aggiorna tag
     */
    Tag update(String userId, String tagId, UpdateTagRequest request);

    /**
     * Elimina tag
     */
    void delete(String userId, String tagId);

    /**
     * Cerca tag per nome parziale
     */
    List<Tag> search(String userId, String searchTerm);

    /**
     * Trova tag più usati
     */
    List<Tag> findMostUsed(String userId, int limit);

    /**
     * Trova tag non usati
     */
    List<Tag> findUnused(String userId);

    /**
     * Conta task per tag
     */
    long countTasksByTag(String tagId);
}
