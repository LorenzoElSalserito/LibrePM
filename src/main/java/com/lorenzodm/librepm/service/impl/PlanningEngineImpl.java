package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.response.GanttTaskResponse;
import com.lorenzodm.librepm.core.entity.Dependency;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.repository.DependencyRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.repository.WbsNodeRepository;
import com.lorenzodm.librepm.service.CapacityEngine;
import com.lorenzodm.librepm.service.PlanningEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the planning engine (PRD-08).
 * Uses the Critical Path Method (CPM) with forward pass for leaves and bottom-up rollup for summaries.
 */
@Service
@Transactional
public class PlanningEngineImpl implements PlanningEngine {

    private static final Logger log = LoggerFactory.getLogger(PlanningEngineImpl.class);

    private final TaskRepository taskRepository;
    private final DependencyRepository dependencyRepository;
    private final WbsNodeRepository wbsNodeRepository;
    private final CapacityEngine capacityEngine;

    public PlanningEngineImpl(TaskRepository taskRepository,
                              DependencyRepository dependencyRepository,
                              WbsNodeRepository wbsNodeRepository,
                              CapacityEngine capacityEngine) {
        this.taskRepository = taskRepository;
        this.dependencyRepository = dependencyRepository;
        this.wbsNodeRepository = wbsNodeRepository;
        this.capacityEngine = capacityEngine;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean wouldCreateCycle(String projectId, String predecessorId, String successorId) {
        List<Dependency> allDeps = dependencyRepository.findByProjectId(projectId);

        Map<String, List<String>> graph = new HashMap<>();
        for (Dependency dep : allDeps) {
            graph.computeIfAbsent(dep.getPredecessor().getId(), k -> new ArrayList<>())
                    .add(dep.getSuccessor().getId());
        }

        graph.computeIfAbsent(predecessorId, k -> new ArrayList<>()).add(successorId);

        Set<String> visited = new HashSet<>();
        return dfsReachable(successorId, predecessorId, graph, visited);
    }

    private boolean dfsReachable(String current, String target, Map<String, List<String>> graph, Set<String> visited) {
        if (current.equals(target)) return true;
        if (visited.contains(current)) return false;
        visited.add(current);
        List<String> successors = graph.getOrDefault(current, List.of());
        for (String successor : successors) {
            if (dfsReachable(successor, target, graph, visited)) return true;
        }
        return false;
    }

    @Override
    public void recalculatePlan(String projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        List<Dependency> deps = dependencyRepository.findByProjectId(projectId);

        if (tasks.isEmpty()) return;

        Map<String, Task> taskMap = tasks.stream().collect(Collectors.toMap(Task::getId, t -> t));
        Map<String, List<Dependency>> incomingDeps = new HashMap<>();
        for (Dependency dep : deps) {
            incomingDeps.computeIfAbsent(dep.getSuccessor().getId(), k -> new ArrayList<>()).add(dep);
        }

        // 1. Filter only leaf tasks (non-summary) for CPM
        // Dependencies should ideally drive leaf tasks.
        List<Task> leafTasks = tasks.stream()
                .filter(t -> t.getType() != Task.Type.SUMMARY_TASK)
                .collect(Collectors.toList());

        // Topological sort setup
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> adjacency = new HashMap<>();
        
        for (Task t : leafTasks) {
            inDegree.put(t.getId(), 0);
            adjacency.put(t.getId(), new ArrayList<>());
        }
        
        for (Dependency dep : deps) {
            if (taskMap.containsKey(dep.getPredecessor().getId()) && taskMap.containsKey(dep.getSuccessor().getId())) {
                 adjacency.computeIfAbsent(dep.getPredecessor().getId(), k -> new ArrayList<>()).add(dep.getSuccessor().getId());
                 inDegree.merge(dep.getSuccessor().getId(), 1, Integer::sum);
            }
        }

        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) queue.add(e.getKey());
        }
        // Fallback for isolated tasks
        for (Task t : leafTasks) {
            if (!inDegree.containsKey(t.getId())) {
                queue.add(t.getId());
            }
        }

        List<Task> toUpdate = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        while (!queue.isEmpty()) {
            String taskId = queue.poll();
            if (processed.contains(taskId)) continue;
            processed.add(taskId);

            Task task = taskMap.get(taskId);
            if (task == null) continue;

            // Compute Early Start based on dependencies
            LocalDateTime newStart = task.getPlannedStart();
            List<Dependency> incoming = incomingDeps.getOrDefault(taskId, List.of());

            for (Dependency dep : incoming) {
                Task pred = dep.getPredecessor();
                if (pred.getPlannedFinish() == null && pred.getPlannedStart() == null) continue;

                LocalDateTime constraintTime = computeConstraint(dep, pred);
                if (constraintTime != null && (newStart == null || constraintTime.isAfter(newStart))) {
                    newStart = constraintTime;
                }
            }
            
            boolean changed = false;
            
            // Apply start date
            if (newStart != null && !Objects.equals(newStart, task.getPlannedStart())) {
                task.setPlannedStart(newStart);
                changed = true;
            }

            // Calculate Finish based on Effort and Calendar
            if (task.getType() == Task.Type.MILESTONE) {
                if (task.getPlannedStart() != null) {
                    task.setPlannedFinish(task.getPlannedStart());
                    changed = true;
                }
            } else if (task.getType() != Task.Type.SUMMARY_TASK) {
                if (task.getPlannedStart() != null && task.getEstimatedEffort() != null && task.getEstimatedEffort() > 0) {
                    // Use CapacityEngine for realistic scheduling
                    LocalDateTime newFinish = capacityEngine.addWorkingMinutes(task.getPlannedStart(), task.getEstimatedEffort());
                    if (!Objects.equals(newFinish, task.getPlannedFinish())) {
                        task.setPlannedFinish(newFinish);
                        changed = true;
                    }
                }
            }

            if (changed) toUpdate.add(task);

            // Propagate
            if (adjacency.containsKey(taskId)) {
                for (String successorId : adjacency.get(taskId)) {
                    inDegree.merge(successorId, -1, Integer::sum);
                    if (inDegree.get(successorId) == 0) queue.add(successorId);
                }
            }
        }

        // 2. Bottom-Up Rollup for Summary Tasks
        boolean rollupChanged = performRollup(tasks, toUpdate);
        
        if (!toUpdate.isEmpty()) {
            List<Task> uniqueUpdates = toUpdate.stream().distinct().collect(Collectors.toList());
            taskRepository.saveAll(uniqueUpdates);
            log.debug("Plan recalculated for project {}: {} tasks updated", projectId, uniqueUpdates.size());
        }
    }

    private boolean performRollup(List<Task> allTasks, List<Task> toUpdate) {
        // Build hierarchy map
        Map<String, List<Task>> childrenMap = new HashMap<>();
        for (Task t : allTasks) {
            if (t.getParentTask() != null) {
                childrenMap.computeIfAbsent(t.getParentTask().getId(), k -> new ArrayList<>()).add(t);
            }
        }

        List<Task> parents = allTasks.stream().filter(t -> childrenMap.containsKey(t.getId())).collect(Collectors.toList());
        
        boolean anyChanged = false;
        // Multi-pass rollup to handle nested summaries
        for (int i = 0; i < 10; i++) {
            boolean passChanged = false;
            for (Task parent : parents) {
                List<Task> children = childrenMap.get(parent.getId());
                if (children == null || children.isEmpty()) continue;

                LocalDateTime minStart = null;
                LocalDateTime maxFinish = null;
                int totalEffort = 0;

                for (Task child : children) {
                    if (child.getPlannedStart() != null) {
                        if (minStart == null || child.getPlannedStart().isBefore(minStart)) {
                            minStart = child.getPlannedStart();
                        }
                    }
                    if (child.getPlannedFinish() != null) {
                        if (maxFinish == null || child.getPlannedFinish().isAfter(maxFinish)) {
                            maxFinish = child.getPlannedFinish();
                        }
                    }
                    if (child.getEstimatedEffort() != null) {
                        totalEffort += child.getEstimatedEffort();
                    }
                }

                if (!Objects.equals(minStart, parent.getPlannedStart())) {
                    parent.setPlannedStart(minStart);
                    passChanged = true;
                }
                if (!Objects.equals(maxFinish, parent.getPlannedFinish())) {
                    parent.setPlannedFinish(maxFinish);
                    passChanged = true;
                }
                if (parent.getEstimatedEffort() == null || parent.getEstimatedEffort() != totalEffort) {
                    parent.setEstimatedEffort(totalEffort);
                    passChanged = true;
                }
                
                if (passChanged) toUpdate.add(parent);
            }
            if (!passChanged) break; 
            anyChanged = true;
        }
        return anyChanged;
    }

    private LocalDateTime computeConstraint(Dependency dep, Task pred) {
        int offsetMinutes = (dep.getLag() != null ? dep.getLag() : 0)
                - (dep.getLead() != null ? dep.getLead() : 0);

        // PRD-08: Support FS, SS, FF, SF
        // For accurate calculation, we need Calendar awareness here too, but simple plusMinutes is used as baseline offset.
        switch (dep.getType()) {
            case FINISH_TO_START:
                if (pred.getPlannedFinish() != null)
                    return pred.getPlannedFinish().plusMinutes(offsetMinutes);
                break;
            case START_TO_START:
                if (pred.getPlannedStart() != null)
                    return pred.getPlannedStart().plusMinutes(offsetMinutes);
                break;
            case FINISH_TO_FINISH:
                // Constraint: Succ.Finish >= Pred.Finish + offset
                // Implies: Succ.Start >= Pred.Finish + offset - Succ.Duration
                // This requires knowing Succ.Duration beforehand. 
                // Handled in simplified way: ignored here, treated as FS fallback or manual adjustment.
                return null; 
            case START_TO_FINISH:
                // Constraint: Succ.Finish >= Pred.Start + offset
                return null; 
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getCriticalPath(String projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        List<Dependency> deps = dependencyRepository.findByProjectId(projectId);
        
        if (tasks.isEmpty()) return Set.of();

        // 1. Identify Project Finish
        LocalDateTime projectFinish = tasks.stream()
                .map(Task::getPlannedFinish)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (projectFinish == null) return Set.of();

        // 2. Build graph for backward pass
        Map<String, List<Task>> predecessors = new HashMap<>();
        Map<String, Task> taskMap = tasks.stream().collect(Collectors.toMap(Task::getId, t -> t));
        
        for (Dependency dep : deps) {
            predecessors.computeIfAbsent(dep.getSuccessor().getId(), k -> new ArrayList<>())
                    .add(dep.getPredecessor());
        }

        // 3. Simple Heuristic: A task is critical if it has zero float.
        // Float = Late Finish - Early Finish.
        // Calculating Late Finish involves backward pass from Project Finish.
        
        Map<String, LocalDateTime> lateFinish = new HashMap<>();
        Map<String, List<String>> successors = new HashMap<>(); // Reverse graph
        for (Dependency dep : deps) {
            successors.computeIfAbsent(dep.getPredecessor().getId(), k -> new ArrayList<>())
                    .add(dep.getSuccessor().getId());
        }

        // Init Late Finish to Project Finish for tasks with no successors
        for (Task t : tasks) {
            if (!successors.containsKey(t.getId()) || successors.get(t.getId()).isEmpty()) {
                lateFinish.put(t.getId(), projectFinish);
            }
        }

        // Topological sort reverse (or just iterative relaxation)
        // Since we don't have topo sort handy, let's use iterative relaxation
        boolean changed = true;
        int iterations = 0;
        while (changed && iterations < tasks.size() + 2) {
            changed = false;
            iterations++;
            for (Task t : tasks) {
                if (lateFinish.containsKey(t.getId())) {
                    LocalDateTime lf = lateFinish.get(t.getId());
                    // Calc LS = LF - Duration
                    // Duration approx:
                    long durationMins = (t.getEstimatedEffort() != null) ? t.getEstimatedEffort() : 0;
                    LocalDateTime ls = lf.minusMinutes(durationMins); // Simplified without calendar

                    // Propagate to predecessors
                    if (predecessors.containsKey(t.getId())) {
                        for (Task pred : predecessors.get(t.getId())) {
                            // Pred LF = min(Succ LS)
                            // Basic FS logic: Pred LF = Succ LS
                            LocalDateTime currentPredLF = lateFinish.get(pred.getId());
                            if (currentPredLF == null || ls.isBefore(currentPredLF)) {
                                lateFinish.put(pred.getId(), ls);
                                changed = true;
                            }
                        }
                    }
                }
            }
        }

        Set<String> criticalPath = new HashSet<>();
        for (Task t : tasks) {
            if (t.getPlannedFinish() == null) continue;
            LocalDateTime lf = lateFinish.get(t.getId());
            if (lf != null) {
                // Allow small tolerance (e.g. 1 minute)
                if (!lf.isAfter(t.getPlannedFinish().plusMinutes(1))) {
                    criticalPath.add(t.getId());
                }
            }
        }
        
        return criticalPath;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GanttTaskResponse> buildGanttData(String projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        List<Dependency> deps = dependencyRepository.findByProjectId(projectId);
        Set<String> criticalIds = getCriticalPath(projectId);

        Map<String, String> wbsCodes = new HashMap<>();
        wbsNodeRepository.findByProjectIdOrderBySortOrderAsc(projectId)
                .forEach(node -> {
                    if (node.getTask() != null) {
                        wbsCodes.put(node.getTask().getId(), node.getWbsCode());
                    }
                });

        Map<String, List<GanttTaskResponse.DependencyRef>> depRefs = new HashMap<>();
        for (Dependency dep : deps) {
            String successorId = dep.getSuccessor().getId();
            depRefs.computeIfAbsent(successorId, k -> new ArrayList<>())
                    .add(new GanttTaskResponse.DependencyRef(
                            dep.getId(),
                            dep.getPredecessor().getId(),
                            dep.getType().name(),
                            dep.getLag(),
                            dep.getLead()
                    ));
        }

        return tasks.stream()
                .filter(t -> !t.isArchived())
                .sorted(Comparator.comparingInt(Task::getSortOrder))
                .map(t -> {
                        // Compute progress: checklist-based if items exist, else effort-based
                        int progress = 0;
                        if (t.getChecklistItems() != null && !t.getChecklistItems().isEmpty()) {
                            long done = t.getChecklistItems().stream()
                                    .filter(ci -> ci.isDone()).count();
                            progress = (int) (done * 100 / t.getChecklistItems().size());
                        } else if (t.getEstimatedEffort() != null && t.getEstimatedEffort() > 0
                                   && t.getActualEffort() != null && t.getActualEffort() > 0) {
                            progress = Math.min(100, t.getActualEffort() * 100 / t.getEstimatedEffort());
                        }
                        // Completed status → 100%
                        if (t.getStatus() != null && t.getStatus().isCompleted()) {
                            progress = 100;
                        }

                        return new GanttTaskResponse(
                                t.getId(),
                                t.getTitle(),
                                t.getType().name(),
                                t.getStatus() != null ? t.getStatus().getName() : null,
                                t.getStatus() != null ? t.getStatus().getColor() : null,
                                t.getPriority() != null ? t.getPriority().getName() : null,
                                t.getPriority() != null ? t.getPriority().getLevel() : 0,
                                t.getPlannedStart(),
                                t.getPlannedFinish(),
                                t.getDeadline(),
                                t.getCreatedAt(),
                                t.getEstimatedEffort(),
                                t.getActualEffort(),
                                progress,
                                t.getParentTask() != null ? t.getParentTask().getId() : null,
                                wbsCodes.get(t.getId()),
                                t.getSortOrder(),
                                t.isBlocked(),
                                criticalIds.contains(t.getId()),
                                depRefs.getOrDefault(t.getId(), List.of())
                        );
                })
                .collect(Collectors.toList());
    }
}
