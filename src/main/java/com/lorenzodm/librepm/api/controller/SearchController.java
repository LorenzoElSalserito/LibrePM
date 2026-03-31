package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<Map<String, List<Map<String, Object>>>> search(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam String q,
            @RequestParam(required = false) List<String> types,
            @RequestParam(required = false) String projectId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(searchService.search(userId, q, types, projectId, limit));
    }
}
