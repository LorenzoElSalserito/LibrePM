package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.repository.ProjectMemberRepository;
import com.lorenzodm.librepm.service.SearchService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final EntityManager em;
    private final ProjectMemberRepository projectMemberRepository;

    public SearchServiceImpl(EntityManager em, ProjectMemberRepository projectMemberRepository) {
        this.em = em;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Override
    public Map<String, List<Map<String, Object>>> search(String userId, String query, List<String> entityTypes, String projectId, int limit) {
        if (query == null || query.isBlank()) {
            return Map.of();
        }

        // Get accessible project IDs for this user
        List<String> accessibleProjectIds = projectMemberRepository.findByUserId(userId).stream()
                .map(pm -> pm.getProject().getId())
                .collect(Collectors.toList());

        if (accessibleProjectIds.isEmpty()) {
            return Map.of();
        }

        // If projectId specified, restrict to that one
        if (projectId != null && !projectId.isBlank()) {
            if (!accessibleProjectIds.contains(projectId)) {
                return Map.of();
            }
            accessibleProjectIds = List.of(projectId);
        }

        boolean searchNotes = entityTypes == null || entityTypes.isEmpty() || entityTypes.contains("notes");
        boolean searchTasks = entityTypes == null || entityTypes.isEmpty() || entityTypes.contains("tasks");

        Map<String, List<Map<String, Object>>> results = new LinkedHashMap<>();

        // Sanitize query for FTS5 (escape special chars, add wildcard)
        String ftsQuery = sanitizeFtsQuery(query);

        if (searchNotes) {
            results.put("notes", searchNotes(ftsQuery, accessibleProjectIds, limit));
        }
        if (searchTasks) {
            results.put("tasks", searchTasks(ftsQuery, accessibleProjectIds, limit));
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> searchNotes(String ftsQuery, List<String> projectIds, int limit) {
        String placeholders = projectIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT n.id, n.title, snippet(notes_fts, 1, '<mark>', '</mark>', '...', 32) as snippet, " +
                "n.note_type, n.project_id, n.parent_type, n.updated_at " +
                "FROM notes n " +
                "JOIN notes_fts ON notes_fts.rowid = n.rowid " +
                "WHERE notes_fts MATCH ? AND n.deleted_at IS NULL " +
                "AND n.project_id IN (" + placeholders + ") " +
                "ORDER BY rank " +
                "LIMIT ?";

        Query q = em.createNativeQuery(sql);
        int idx = 1;
        q.setParameter(idx++, ftsQuery);
        for (String pid : projectIds) {
            q.setParameter(idx++, pid);
        }
        q.setParameter(idx, limit);

        List<Object[]> rows;
        try {
            rows = q.getResultList();
        } catch (Exception e) {
            // FTS5 might not be available or query syntax error - fallback to LIKE
            return searchNotesLike(ftsQuery, projectIds, limit);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", row[0]);
            map.put("title", row[1]);
            map.put("snippet", row[2]);
            map.put("noteType", row[3]);
            map.put("projectId", row[4]);
            map.put("parentType", row[5]);
            map.put("updatedAt", row[6]);
            map.put("entityType", "NOTE");
            results.add(map);
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> searchTasks(String ftsQuery, List<String> projectIds, int limit) {
        String placeholders = projectIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT t.id, t.title, snippet(tasks_fts, 1, '<mark>', '</mark>', '...', 32) as snippet, " +
                "t.status, t.project_id, t.priority, t.updated_at " +
                "FROM tasks t " +
                "JOIN tasks_fts ON tasks_fts.rowid = t.rowid " +
                "WHERE tasks_fts MATCH ? AND t.deleted_at IS NULL " +
                "AND t.project_id IN (" + placeholders + ") " +
                "ORDER BY rank " +
                "LIMIT ?";

        Query q = em.createNativeQuery(sql);
        int idx = 1;
        q.setParameter(idx++, ftsQuery);
        for (String pid : projectIds) {
            q.setParameter(idx++, pid);
        }
        q.setParameter(idx, limit);

        List<Object[]> rows;
        try {
            rows = q.getResultList();
        } catch (Exception e) {
            // Fallback to LIKE
            return searchTasksLike(ftsQuery, projectIds, limit);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", row[0]);
            map.put("title", row[1]);
            map.put("snippet", row[2]);
            map.put("status", row[3]);
            map.put("projectId", row[4]);
            map.put("priority", row[5]);
            map.put("updatedAt", row[6]);
            map.put("entityType", "TASK");
            results.add(map);
        }
        return results;
    }

    // Fallback methods using LIKE for environments where FTS5 is not available
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> searchNotesLike(String query, List<String> projectIds, int limit) {
        String likeQuery = "%" + query.replace("*", "") + "%";
        String placeholders = projectIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, title, SUBSTR(content, 1, 100) as snippet, note_type, project_id, parent_type, updated_at " +
                "FROM notes WHERE deleted_at IS NULL AND project_id IN (" + placeholders + ") " +
                "AND (title LIKE ? OR content LIKE ?) ORDER BY updated_at DESC LIMIT ?";

        Query q = em.createNativeQuery(sql);
        int idx = 1;
        for (String pid : projectIds) { q.setParameter(idx++, pid); }
        q.setParameter(idx++, likeQuery);
        q.setParameter(idx++, likeQuery);
        q.setParameter(idx, limit);

        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> results = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", row[0]); map.put("title", row[1]); map.put("snippet", row[2]);
            map.put("noteType", row[3]); map.put("projectId", row[4]); map.put("parentType", row[5]);
            map.put("updatedAt", row[6]); map.put("entityType", "NOTE");
            results.add(map);
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> searchTasksLike(String query, List<String> projectIds, int limit) {
        String likeQuery = "%" + query.replace("*", "") + "%";
        String placeholders = projectIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, title, SUBSTR(description, 1, 100) as snippet, status, project_id, priority, updated_at " +
                "FROM tasks WHERE deleted_at IS NULL AND project_id IN (" + placeholders + ") " +
                "AND (title LIKE ? OR description LIKE ?) ORDER BY updated_at DESC LIMIT ?";

        Query q = em.createNativeQuery(sql);
        int idx = 1;
        for (String pid : projectIds) { q.setParameter(idx++, pid); }
        q.setParameter(idx++, likeQuery);
        q.setParameter(idx++, likeQuery);
        q.setParameter(idx, limit);

        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> results = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", row[0]); map.put("title", row[1]); map.put("snippet", row[2]);
            map.put("status", row[3]); map.put("projectId", row[4]); map.put("priority", row[5]);
            map.put("updatedAt", row[6]); map.put("entityType", "TASK");
            results.add(map);
        }
        return results;
    }

    private String sanitizeFtsQuery(String query) {
        // Escape FTS5 special characters and add prefix matching
        String cleaned = query.replaceAll("[\"(){}\\[\\]^~*:]", " ").trim();
        if (cleaned.isEmpty()) return "\"\"";
        // Add wildcard suffix for prefix matching
        String[] terms = cleaned.split("\\s+");
        return Arrays.stream(terms)
                .filter(t -> !t.isEmpty())
                .map(t -> "\"" + t + "\"*")
                .collect(Collectors.joining(" "));
    }
}
