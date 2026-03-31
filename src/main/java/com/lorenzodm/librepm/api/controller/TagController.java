package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateTagRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTagRequest;
import com.lorenzodm.librepm.api.dto.response.TagResponse;
import com.lorenzodm.librepm.api.mapper.TagMapper;
import com.lorenzodm.librepm.core.entity.Tag;
import com.lorenzodm.librepm.security.CurrentUser;
import com.lorenzodm.librepm.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST per gestione Tag
 * 
 * Endpoints:
 * - POST   /api/tags          - Crea nuovo tag
 * - GET    /api/tags          - Lista tutti i tag dell'utente
 * - GET    /api/tags/{id}     - Dettagli tag
 * - PUT    /api/tags/{id}     - Aggiorna tag
 * - DELETE /api/tags/{id}     - Elimina tag
 * - GET    /api/tags/search   - Cerca tag per nome
 * - GET    /api/tags/stats    - Statistiche tag
 *
 * @author Lorenzo DM
 * @since 0.2.0
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;
    private final TagMapper tagMapper;

    public TagController(TagService tagService, TagMapper tagMapper) {
        this.tagService = tagService;
        this.tagMapper = tagMapper;
    }

    /**
     * Crea nuovo tag
     */
    @PostMapping
    public ResponseEntity<TagResponse> create(
            @CurrentUser String userId,
            @Valid @RequestBody CreateTagRequest request) {
        
        Tag tag = tagService.create(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tagMapper.toResponse(tag));
    }

    /**
     * Lista tutti i tag dell'utente
     */
    @GetMapping
    public ResponseEntity<List<TagResponse>> list(@CurrentUser String userId) {
        List<Tag> tags = tagService.listOwned(userId);
        List<TagResponse> response = tags.stream()
                .map(tagMapper::toResponse)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Dettagli tag per ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getById(
            @CurrentUser String userId,
            @PathVariable String id) {
        
        Tag tag = tagService.getOwned(userId, id);
        return ResponseEntity.ok(tagMapper.toResponse(tag));
    }

    /**
     * Aggiorna tag
     */
    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> update(
            @CurrentUser String userId,
            @PathVariable String id,
            @Valid @RequestBody UpdateTagRequest request) {
        
        Tag tag = tagService.update(userId, id, request);
        return ResponseEntity.ok(tagMapper.toResponse(tag));
    }

    /**
     * Elimina tag
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @CurrentUser String userId,
            @PathVariable String id) {
        
        tagService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cerca tag per nome
     */
    @GetMapping("/search")
    public ResponseEntity<List<TagResponse>> search(
            @CurrentUser String userId,
            @RequestParam(required = false) String q) {
        
        List<Tag> tags = tagService.search(userId, q);
        List<TagResponse> response = tags.stream()
                .map(tagMapper::toResponseLight) // Usa versione light per performance
                .toList();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Statistiche tag
     * Ritorna tag più usati e non usati
     */
    @GetMapping("/stats")
    public ResponseEntity<TagStatsResponse> getStats(@CurrentUser String userId) {
        List<Tag> mostUsed = tagService.findMostUsed(userId, 10);
        List<Tag> unused = tagService.findUnused(userId);
        
        List<TagResponse> mostUsedResponse = mostUsed.stream()
                .map(tagMapper::toResponse)
                .toList();
        
        List<TagResponse> unusedResponse = unused.stream()
                .map(tagMapper::toResponseLight)
                .toList();
        
        return ResponseEntity.ok(new TagStatsResponse(mostUsedResponse, unusedResponse));
    }

    /**
     * DTO interno per stats
     */
    private record TagStatsResponse(
            List<TagResponse> mostUsed,
            List<TagResponse> unused
    ) {}
}
