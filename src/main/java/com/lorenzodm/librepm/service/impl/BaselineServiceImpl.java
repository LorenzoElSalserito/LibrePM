package com.lorenzodm.librepm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorenzodm.librepm.api.dto.request.CreateBaselineRequest;
import com.lorenzodm.librepm.api.exception.BadRequestException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.*;
import com.lorenzodm.librepm.repository.BaselineRepository;
import com.lorenzodm.librepm.repository.DeliverableRepository;
import com.lorenzodm.librepm.repository.ProjectRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.service.BaselineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BaselineServiceImpl implements BaselineService {

    private final BaselineRepository baselineRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final DeliverableRepository deliverableRepository;
    private final ObjectMapper objectMapper;

    public BaselineServiceImpl(BaselineRepository baselineRepository,
                               ProjectRepository projectRepository,
                               TaskRepository taskRepository,
                               DeliverableRepository deliverableRepository,
                               ObjectMapper objectMapper) {
        this.baselineRepository = baselineRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.deliverableRepository = deliverableRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * PRD-11-FR-001: Create immutable baseline snapshot.
     * PRD-11-BR-001: Baselines MUST NOT be modifiable (entity has no update method).
     */
    @Override
    public Baseline create(String userId, String projectId, CreateBaselineRequest req) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Progetto non trovato"));

        Baseline baseline = new Baseline();
        baseline.setProject(project);
        baseline.setName(req.name());
        baseline.setSnapshotDate(LocalDateTime.now());
        baseline.setFrozen(true);

        // Snapshot deliverables as JSON
        try {
            List<Deliverable> deliverables = deliverableRepository.findByProjectId(projectId);
            List<Map<String, Object>> delSnap = deliverables.stream().map(d -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", d.getId());
                m.put("name", d.getName());
                m.put("riskStatus", d.getRiskStatus() != null ? d.getRiskStatus().name() : null);
                return m;
            }).toList();
            baseline.setDeliverableSnapshot(objectMapper.writeValueAsString(delSnap));
        } catch (Exception e) {
            baseline.setDeliverableSnapshot("[]");
        }

        baseline = baselineRepository.save(baseline);

        // Snapshot all tasks with their current planning data
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        for (Task task : tasks) {
            BaselineTaskSnapshot snapshot = new BaselineTaskSnapshot();
            snapshot.setBaseline(baseline);
            snapshot.setTask(task);
            snapshot.setPlannedStart(task.getPlannedStart());
            snapshot.setPlannedFinish(task.getPlannedFinish());
            snapshot.setEstimatedEffort(task.getEstimatedEffort());
            baseline.getTaskSnapshots().add(snapshot);
        }

        return baselineRepository.save(baseline);
    }

    @Override
    @Transactional(readOnly = true)
    public Baseline getById(String userId, String projectId, String baselineId) {
        return baselineRepository.findById(baselineId)
                .filter(b -> b.getProject().getId().equals(projectId))
                .orElseThrow(() -> new ResourceNotFoundException("Baseline non trovata"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Baseline> listByProject(String userId, String projectId) {
        return baselineRepository.findByProjectIdOrderBySnapshotDateDesc(projectId);
    }

    @Override
    public void delete(String userId, String projectId, String baselineId) {
        Baseline baseline = getById(userId, projectId, baselineId);
        if (baseline.isFrozen()) {
            throw new BadRequestException("Cannot delete a frozen baseline. Unfreeze it first.");
        }
        baselineRepository.delete(baseline);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> compare(String userId, String projectId, String baselineId) {
        Baseline baseline = getById(userId, projectId, baselineId);
        List<Task> currentTasks = taskRepository.findByProjectId(projectId);

        // Build map of current tasks by ID
        Map<String, Task> currentMap = new LinkedHashMap<>();
        for (Task t : currentTasks) currentMap.put(t.getId(), t);

        List<Map<String, Object>> deviations = new java.util.ArrayList<>();
        for (BaselineTaskSnapshot snap : baseline.getTaskSnapshots()) {
            Task current = currentMap.get(snap.getTask().getId());
            if (current == null) {
                deviations.add(Map.of("taskId", snap.getTask().getId(), "type", "REMOVED"));
                continue;
            }
            Map<String, Object> dev = new LinkedHashMap<>();
            dev.put("taskId", current.getId());
            dev.put("taskTitle", current.getTitle());

            boolean changed = false;
            if (snap.getPlannedStart() != null && current.getPlannedStart() != null
                    && !snap.getPlannedStart().equals(current.getPlannedStart())) {
                dev.put("startBaseline", snap.getPlannedStart().toString());
                dev.put("startCurrent", current.getPlannedStart().toString());
                changed = true;
            }
            if (snap.getPlannedFinish() != null && current.getPlannedFinish() != null
                    && !snap.getPlannedFinish().equals(current.getPlannedFinish())) {
                dev.put("finishBaseline", snap.getPlannedFinish().toString());
                dev.put("finishCurrent", current.getPlannedFinish().toString());
                changed = true;
            }
            if (snap.getEstimatedEffort() != null && current.getEstimatedEffort() != null
                    && !snap.getEstimatedEffort().equals(current.getEstimatedEffort())) {
                dev.put("effortBaseline", snap.getEstimatedEffort());
                dev.put("effortCurrent", current.getEstimatedEffort());
                changed = true;
            }
            if (changed) {
                dev.put("type", "CHANGED");
                deviations.add(dev);
            }
            currentMap.remove(current.getId());
        }

        // Tasks added since baseline
        for (Task added : currentMap.values()) {
            deviations.add(Map.of("taskId", added.getId(), "taskTitle", added.getTitle(), "type", "ADDED"));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("baselineId", baselineId);
        result.put("baselineName", baseline.getName());
        result.put("snapshotDate", baseline.getSnapshotDate().toString());
        result.put("deviations", deviations);
        result.put("deviationCount", deviations.size());
        return result;
    }
}
