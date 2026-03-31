package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateNoteRequest;
import com.lorenzodm.librepm.api.dto.response.NoteResponse;
import com.lorenzodm.librepm.api.mapper.NoteMapper;
import com.lorenzodm.librepm.core.entity.Note;
import com.lorenzodm.librepm.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ContextNoteController {

    private final NoteService noteService;
    private final NoteMapper noteMapper;

    public ContextNoteController(NoteService noteService, NoteMapper noteMapper) {
        this.noteService = noteService;
        this.noteMapper = noteMapper;
    }

    // =================================================================================
    // TASK CONTEXT
    // =================================================================================

    @GetMapping("/tasks/{taskId}/notes")
    public ResponseEntity<List<NoteResponse>> listForTask(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String taskId) {
        
        List<Note> notes = noteService.listForTask(userId, taskId);
        return ResponseEntity.ok(notes.stream().map(noteMapper::toResponse).collect(Collectors.toList()));
    }

    @PostMapping("/tasks/{taskId}/notes")
    public ResponseEntity<NoteResponse> createForTask(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String taskId,
            @Valid @RequestBody CreateNoteRequest request) {
        
        Note created = noteService.createForTask(userId, taskId, request);
        return ResponseEntity.created(URI.create("/api/notes/" + created.getId()))
                .body(noteMapper.toResponse(created));
    }

    // =================================================================================
    // PROJECT CONTEXT
    // =================================================================================

    @GetMapping("/projects/{projectId}/notes")
    public ResponseEntity<List<NoteResponse>> listForProject(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String projectId) {
        
        List<Note> notes = noteService.listForProject(userId, projectId);
        return ResponseEntity.ok(notes.stream().map(noteMapper::toResponse).collect(Collectors.toList()));
    }

    @PostMapping("/projects/{projectId}/notes")
    public ResponseEntity<NoteResponse> createForProject(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String projectId,
            @Valid @RequestBody CreateNoteRequest request) {
        
        Note created = noteService.createForProject(userId, projectId, request);
        return ResponseEntity.created(URI.create("/api/notes/" + created.getId()))
                .body(noteMapper.toResponse(created));
    }
}
