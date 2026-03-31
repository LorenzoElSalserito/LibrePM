package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateWbsNodeRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateWbsNodeRequest;
import com.lorenzodm.librepm.api.dto.response.WbsNodeResponse;
import com.lorenzodm.librepm.api.mapper.WbsNodeMapper;
import com.lorenzodm.librepm.service.WbsNodeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/projects/{projectId}/wbs")
public class WbsNodeController {

    private final WbsNodeService wbsNodeService;
    private final WbsNodeMapper wbsNodeMapper;

    public WbsNodeController(WbsNodeService wbsNodeService, WbsNodeMapper wbsNodeMapper) {
        this.wbsNodeService = wbsNodeService;
        this.wbsNodeMapper = wbsNodeMapper;
    }

    @PostMapping
    public ResponseEntity<WbsNodeResponse> create(
            @PathVariable String userId,
            @PathVariable String projectId,
            @Valid @RequestBody CreateWbsNodeRequest req
    ) {
        return ResponseEntity.ok(wbsNodeMapper.toResponse(
                wbsNodeService.create(userId, projectId, req)
        ));
    }

    @GetMapping
    public ResponseEntity<List<WbsNodeResponse>> listRoots(
            @PathVariable String userId,
            @PathVariable String projectId,
            @RequestParam(defaultValue = "false") boolean flat
    ) {
        if (flat) {
            return ResponseEntity.ok(
                    wbsNodeService.listAll(userId, projectId).stream()
                            .map(wbsNodeMapper::toResponseFlat).toList()
            );
        }
        return ResponseEntity.ok(
                wbsNodeService.listRoots(userId, projectId).stream()
                        .map(wbsNodeMapper::toResponse).toList()
        );
    }

    @GetMapping("/{nodeId}")
    public ResponseEntity<WbsNodeResponse> get(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String nodeId
    ) {
        return ResponseEntity.ok(wbsNodeMapper.toResponse(
                wbsNodeService.getById(userId, projectId, nodeId)
        ));
    }

    @PutMapping("/{nodeId}")
    public ResponseEntity<WbsNodeResponse> update(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String nodeId,
            @Valid @RequestBody UpdateWbsNodeRequest req
    ) {
        return ResponseEntity.ok(wbsNodeMapper.toResponse(
                wbsNodeService.update(userId, projectId, nodeId, req)
        ));
    }

    @DeleteMapping("/{nodeId}")
    public ResponseEntity<Void> delete(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String nodeId
    ) {
        wbsNodeService.delete(userId, projectId, nodeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/regenerate-codes")
    public ResponseEntity<Void> regenerateCodes(
            @PathVariable String userId,
            @PathVariable String projectId
    ) {
        wbsNodeService.regenerateCodes(projectId);
        return ResponseEntity.noContent().build();
    }
}
