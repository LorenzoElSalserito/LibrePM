package com.lorenzodm.librepm.service;

import java.util.List;
import java.util.Map;

public interface SearchService {

    /**
     * Performs full-text search across notes and tasks (PRD-02-FR-004).
     *
     * @param userId      the current user ID
     * @param query       the search query
     * @param entityTypes list of entity types to search (notes, tasks); null = all
     * @param projectId   optional project filter
     * @param limit       max results per type
     * @return map of entity type to list of result maps
     */
    Map<String, List<Map<String, Object>>> search(String userId, String query, List<String> entityTypes, String projectId, int limit);
}
