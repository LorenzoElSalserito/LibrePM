package com.lorenzodm.librepm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.lorenzodm.librepm.api.dto.request.CreateProjectRequest;
import com.lorenzodm.librepm.api.dto.request.CreateTagRequest;
import com.lorenzodm.librepm.api.dto.request.CreateTaskChecklistItemRequest;
import com.lorenzodm.librepm.api.dto.request.CreateTaskRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTaskChecklistItemRequest;
import com.lorenzodm.librepm.api.dto.response.TaskChecklistItemResponse;
import com.lorenzodm.librepm.api.exception.BadRequestException;
import com.lorenzodm.librepm.core.entity.Project;
import com.lorenzodm.librepm.core.entity.Tag;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.core.entity.TaskChecklistItem;
import com.lorenzodm.librepm.repository.TagRepository;
import com.lorenzodm.librepm.repository.TaskPriorityRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.repository.TaskStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Portable JSON export/import of a user's workspace (projects, tasks with hierarchy, tags,
 * checklists). Unlike the raw .db transfer it is database-engine agnostic, so it works on both the
 * local SQLite database and a shared MariaDB server.
 *
 * Import is additive: projects in the file are created as new projects with fresh ids; nothing
 * that already exists is modified or deleted. The whole import runs in one transaction, so an
 * invalid file leaves the workspace untouched.
 */
@Service
public class WorkspaceTransferService {

    public static final String FORMAT = "librepm-workspace";
    public static final int FORMAT_VERSION = 1;

    private final ProjectService projectService;
    private final TaskService taskService;
    private final TagService tagService;
    private final TaskChecklistService checklistService;
    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final TaskPriorityRepository taskPriorityRepository;
    private final TagRepository tagRepository;

    public WorkspaceTransferService(ProjectService projectService,
                                    TaskService taskService,
                                    TagService tagService,
                                    TaskChecklistService checklistService,
                                    TaskRepository taskRepository,
                                    TaskStatusRepository taskStatusRepository,
                                    TaskPriorityRepository taskPriorityRepository,
                                    TagRepository tagRepository) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.tagService = tagService;
        this.checklistService = checklistService;
        this.taskRepository = taskRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.taskPriorityRepository = taskPriorityRepository;
        this.tagRepository = tagRepository;
    }

    public record ImportSummary(int projects, int tasks, int tags, int checklistItems, List<String> warnings) {
    }

    // ------------------------------------------------------------------ export

    @Transactional(readOnly = true)
    public Map<String, Object> export(String userId) {
        List<Project> projects = projectService.listOwned(userId, null, null, null);

        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("format", FORMAT);
        doc.put("formatVersion", FORMAT_VERSION);
        doc.put("exportedAt", Instant.now().toString());

        Set<String> tagNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Map<String, String> tagColors = new HashMap<>();
        List<Map<String, Object>> projectList = new ArrayList<>();
        int taskCount = 0;

        for (Project p : projects) {
            Map<String, Object> pm = new LinkedHashMap<>();
            pm.put("ref", p.getId());
            pm.put("name", p.getName());
            pm.put("description", p.getDescription());
            pm.put("color", p.getColor());
            pm.put("icon", p.getIcon());
            pm.put("favorite", p.isFavorite());
            pm.put("archived", p.isArchived());

            List<Task> tasks = taskRepository.findByProjectIdOrderBySortOrderAsc(p.getId());
            List<Map<String, Object>> taskList = new ArrayList<>();
            for (Task t : tasks) {
                Map<String, Object> tm = new LinkedHashMap<>();
                tm.put("ref", t.getId());
                tm.put("parentRef", t.getParentTask() != null ? t.getParentTask().getId() : null);
                tm.put("title", t.getTitle());
                tm.put("description", t.getDescription());
                tm.put("status", t.getStatus() != null ? t.getStatus().getName() : null);
                tm.put("priority", t.getPriority() != null ? t.getPriority().getName() : null);
                tm.put("type", t.getType() != null ? t.getType().name() : null);
                tm.put("deadline", t.getDeadline() != null ? t.getDeadline().toString() : null);
                tm.put("plannedStart", t.getPlannedStart() != null ? t.getPlannedStart().toString() : null);
                tm.put("plannedFinish", t.getPlannedFinish() != null ? t.getPlannedFinish().toString() : null);
                tm.put("owner", t.getOwner());
                tm.put("notes", t.getNotes());
                tm.put("markdownNotes", t.getMarkdownNotes());
                tm.put("sortOrder", t.getSortOrder());
                tm.put("estimatedEffort", t.getEstimatedEffort());
                tm.put("actualEffort", t.getActualEffort());
                tm.put("archived", t.isArchived());

                List<String> tags = new ArrayList<>();
                for (Tag tag : t.getTags()) {
                    tags.add(tag.getName());
                    tagNames.add(tag.getName());
                    if (tag.getColor() != null) tagColors.putIfAbsent(tag.getName().toLowerCase(Locale.ROOT), tag.getColor());
                }
                Collections.sort(tags);
                tm.put("tags", tags);

                List<Map<String, Object>> checklist = new ArrayList<>();
                for (TaskChecklistItem item : t.getChecklistItems()) {
                    Map<String, Object> cm = new LinkedHashMap<>();
                    cm.put("text", item.getText());
                    cm.put("done", item.isDone());
                    cm.put("sortOrder", item.getSortOrder());
                    checklist.add(cm);
                }
                tm.put("checklist", checklist);
                taskList.add(tm);
            }
            taskCount += taskList.size();
            pm.put("tasks", taskList);
            projectList.add(pm);
        }

        List<Map<String, Object>> tagList = new ArrayList<>();
        for (String name : tagNames) {
            Map<String, Object> tm = new LinkedHashMap<>();
            tm.put("name", name);
            tm.put("color", tagColors.get(name.toLowerCase(Locale.ROOT)));
            tagList.add(tm);
        }

        doc.put("summary", Map.of("projects", projectList.size(), "tasks", taskCount, "tags", tagList.size()));
        doc.put("tags", tagList);
        doc.put("projects", projectList);
        return doc;
    }

    // ------------------------------------------------------------------ import

    @Transactional
    public ImportSummary importWorkspace(String userId, JsonNode doc) {
        if (doc == null || !doc.isObject()) {
            throw new BadRequestException("Invalid file: expected a LibrePM JSON export");
        }
        String format = text(doc, "format");
        if (format != null && !FORMAT.equals(format)) {
            throw new BadRequestException("Unsupported file format: " + format);
        }
        int version = doc.path("formatVersion").asInt(FORMAT_VERSION);
        if (version > FORMAT_VERSION) {
            throw new BadRequestException("This export was created by a newer LibrePM version (format "
                    + version + "). Update LibrePM first.");
        }
        JsonNode projects = doc.get("projects");
        if (projects == null || !projects.isArray()) {
            throw new BadRequestException("Invalid file: the 'projects' list is missing");
        }

        // Tags: reuse the user's existing tags by name, create the missing ones.
        Map<String, String> tagIds = new HashMap<>();
        for (Tag existing : tagService.listOwned(userId)) {
            tagIds.put(existing.getName().toLowerCase(Locale.ROOT), existing.getId());
        }
        Map<String, String> declaredColors = new HashMap<>();
        for (JsonNode tag : iterable(doc.get("tags"))) {
            String name = text(tag, "name");
            if (name != null) declaredColors.put(name.trim().toLowerCase(Locale.ROOT), text(tag, "color"));
        }
        int createdTags = 0;
        Set<String> unavailableTags = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        int createdProjects = 0;
        int createdTasks = 0;
        int createdChecklist = 0;

        for (JsonNode pn : projects) {
            String name = text(pn, "name");
            if (name == null || name.isBlank()) {
                throw new BadRequestException("Invalid file: a project has no name");
            }
            Project project = projectService.create(userId, new CreateProjectRequest(
                    truncate(name, 200), truncate(text(pn, "description"), 5000), truncate(text(pn, "color"), 20),
                    truncate(text(pn, "icon"), 50), pn.path("favorite").asBoolean(false), null, null));
            createdProjects++;

            // Create parents before children: order the tasks so every parentRef is already mapped.
            List<JsonNode> pendingTasks = new ArrayList<>();
            iterable(pn.get("tasks")).forEach(pendingTasks::add);
            Map<String, String> refToId = new HashMap<>();
            Set<String> knownRefs = new HashSet<>();
            for (JsonNode tn : pendingTasks) {
                String ref = text(tn, "ref");
                if (ref != null) knownRefs.add(ref);
            }

            while (!pendingTasks.isEmpty()) {
                int before = pendingTasks.size();
                Iterator<JsonNode> it = pendingTasks.iterator();
                while (it.hasNext()) {
                    JsonNode tn = it.next();
                    String parentRef = text(tn, "parentRef");
                    boolean parentReady = parentRef == null || !knownRefs.contains(parentRef) || refToId.containsKey(parentRef);
                    if (!parentReady) continue;

                    List<String> taskTagIds = new ArrayList<>();
                    for (JsonNode tagName : iterable(tn.get("tags"))) {
                        String tName = tagName.asText("").trim();
                        if (tName.isEmpty()) continue;
                        String key = tName.toLowerCase(Locale.ROOT);
                        String tagId = tagIds.get(key);
                        if (tagId == null && unavailableTags.contains(tName)) continue;
                        if (tagId == null && tagRepository.findByNameIgnoreCase(tName).isPresent()) {
                            // Tag names are unique across the whole database: another user owns it.
                            unavailableTags.add(tName);
                            continue;
                        }
                        if (tagId == null) {
                            Tag created = tagService.create(userId, new CreateTagRequest(tName, declaredColors.get(key)));
                            tagId = created.getId();
                            tagIds.put(key, tagId);
                            createdTags++;
                        }
                        if (!taskTagIds.contains(tagId)) taskTagIds.add(tagId);
                    }

                    String title = text(tn, "title");
                    if (title == null || title.isBlank()) {
                        throw new BadRequestException("Invalid file: a task in project '" + name + "' has no title");
                    }

                    Task task = taskService.create(userId, project.getId(), new CreateTaskRequest(
                            truncate(title, 500),
                            truncate(text(tn, "description"), 5000),
                            statusId(text(tn, "status")),
                            priorityId(text(tn, "priority")),
                            date(text(tn, "deadline")),
                            truncate(text(tn, "owner"), 200),
                            truncate(text(tn, "notes"), 2000),
                            text(tn, "markdownNotes"),
                            tn.hasNonNull("sortOrder") ? tn.get("sortOrder").asInt() : null,
                            null, null, null,
                            tn.hasNonNull("estimatedEffort") ? tn.get("estimatedEffort").asInt() : null,
                            tn.hasNonNull("actualEffort") ? tn.get("actualEffort").asInt() : null,
                            null, null, null, null,
                            taskTagIds,
                            text(tn, "type"),
                            dateTime(text(tn, "plannedStart")),
                            dateTime(text(tn, "plannedFinish")),
                            parentRef != null ? refToId.get(parentRef) : null,
                            null, null, null, null, null, null, null, null, null, null));
                    createdTasks++;
                    String ref = text(tn, "ref");
                    if (ref != null) refToId.put(ref, task.getId());

                    if (tn.path("archived").asBoolean(false)) {
                        taskService.setArchived(userId, project.getId(), task.getId(), true);
                    }

                    for (JsonNode cn : iterable(tn.get("checklist"))) {
                        String text = text(cn, "text");
                        if (text == null || text.isBlank()) continue;
                        TaskChecklistItemResponse item = checklistService.createItem(userId, project.getId(), task.getId(),
                                new CreateTaskChecklistItemRequest(truncate(text, 1000),
                                        cn.hasNonNull("sortOrder") ? cn.get("sortOrder").asInt() : null));
                        if (cn.path("done").asBoolean(false)) {
                            checklistService.updateItem(userId, project.getId(), task.getId(), item.id(),
                                    new UpdateTaskChecklistItemRequest(null, true, null));
                        }
                        createdChecklist++;
                    }
                    it.remove();
                }
                if (pendingTasks.size() == before) {
                    throw new BadRequestException("Invalid file: circular task hierarchy in project '" + name + "'");
                }
            }

            if (pn.path("archived").asBoolean(false)) {
                projectService.setArchived(userId, project.getId(), true);
            }
        }

        List<String> warnings = new ArrayList<>();
        for (String tag : unavailableTags) {
            warnings.add("Tag '" + tag + "' not applied: the name is already used by another profile");
        }
        return new ImportSummary(createdProjects, createdTasks, createdTags, createdChecklist, warnings);
    }

    // ------------------------------------------------------------------ helpers

    private String statusId(String name) {
        if (name == null || name.isBlank()) return null;
        return taskStatusRepository.findByNameIgnoreCase(name.trim()).map(s -> s.getId()).orElse(null);
    }

    private String priorityId(String name) {
        if (name == null || name.isBlank()) return null;
        return taskPriorityRepository.findByNameIgnoreCase(name.trim()).map(p -> p.getId()).orElse(null);
    }

    private static String text(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    private static Iterable<JsonNode> iterable(JsonNode node) {
        if (node == null || !node.isArray()) return List.of();
        return node;
    }

    private static String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static LocalDate date(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value.length() > 10 ? value.substring(0, 10) : value);
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDateTime dateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            LocalDate d = date(value);
            return d != null ? d.atStartOfDay() : null;
        }
    }
}
