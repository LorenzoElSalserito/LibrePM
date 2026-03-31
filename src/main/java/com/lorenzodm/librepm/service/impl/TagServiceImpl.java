package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateTagRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTagRequest;
import com.lorenzodm.librepm.api.exception.ConflictException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.Tag;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.TagRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementazione TagService
 *
 * @author Lorenzo DM
 * @since 0.2.0
 */
@Service
@Transactional
public class TagServiceImpl implements TagService {

    private static final Logger log = LoggerFactory.getLogger(TagServiceImpl.class);

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    public TagServiceImpl(TagRepository tagRepository, UserRepository userRepository) {
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Tag create(String userId, CreateTagRequest request) {
        log.debug("Creating tag for user {}: {}", userId, request.name());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User non trovato: " + userId));

        // Verifica se esiste già un tag con quel nome per questo utente
        if (tagRepository.existsByNameAndOwnerId(request.name(), userId)) {
            throw new ConflictException("Esiste già un tag con nome: " + request.name());
        }

        Tag tag = new Tag();
        tag.setName(request.name().trim());
        tag.setColor(request.color());
        tag.setOwner(user);

        return tagRepository.save(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public Tag getOwned(String userId, String tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag non trovato: " + tagId));

        // Verifica ownership
        if (!tag.getOwner().getId().equals(userId)) {
            throw new ResourceNotFoundException("Tag non trovato: " + tagId);
        }

        return tag;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> listOwned(String userId) {
        return tagRepository.findByOwnerIdOrderByNameAsc(userId);
    }

    @Override
    public Tag update(String userId, String tagId, UpdateTagRequest request) {
        log.debug("Updating tag {} for user {}", tagId, userId);

        Tag tag = getOwned(userId, tagId);

        if (request.name() != null && !request.name().isBlank()) {
            String newName = request.name().trim();
            // Verifica se il nuovo nome è già usato da un altro tag
            if (!tag.getName().equals(newName) && 
                tagRepository.existsByNameAndOwnerId(newName, userId)) {
                throw new ConflictException("Esiste già un tag con nome: " + newName);
            }
            tag.setName(newName);
        }

        if (request.color() != null) {
            tag.setColor(request.color());
        }

        return tagRepository.save(tag);
    }

    @Override
    public void delete(String userId, String tagId) {
        log.debug("Deleting tag {} for user {}", tagId, userId);

        Tag tag = getOwned(userId, tagId);
        
        // Rimuovi il tag da tutti i task associati
        tag.getTasks().forEach(task -> task.getTags().remove(tag));
        
        tagRepository.delete(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> search(String userId, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return listOwned(userId);
        }
        return tagRepository.searchByName(userId, searchTerm.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> findMostUsed(String userId, int limit) {
        return tagRepository.findMostUsedTags(userId).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> findUnused(String userId) {
        return tagRepository.findUnusedTags(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTasksByTag(String tagId) {
        return tagRepository.countTasksByTagId(tagId);
    }
}
