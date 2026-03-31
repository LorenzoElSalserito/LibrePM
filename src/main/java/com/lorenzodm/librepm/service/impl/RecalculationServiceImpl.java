package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.core.entity.Dependency;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.repository.DependencyRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.service.EventJournalService;
import com.lorenzodm.librepm.service.RecalculationService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class RecalculationServiceImpl implements RecalculationService {

    private static final Logger log = LoggerFactory.getLogger(RecalculationServiceImpl.class);

    private final TaskRepository taskRepository;
    private final DependencyRepository dependencyRepository;
    private final EventJournalService eventJournalService;

    public RecalculationServiceImpl(TaskRepository taskRepository,
                                    DependencyRepository dependencyRepository,
                                    EventJournalService eventJournalService) {
        this.taskRepository = taskRepository;
        this.dependencyRepository = dependencyRepository;
        this.eventJournalService = eventJournalService;
    }

    @Override
    public void recalculate(String projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        if (tasks.isEmpty()) return;

        List<Dependency> dependencies = dependencyRepository.findByProjectId(projectId);

        // Build adjacency maps
        Map<String, Task> taskMap = new HashMap<>();
        Map<String, List<String>> predecessors = new HashMap<>(); // taskId -> list of predecessor taskIds
        Map<String, List<String>> successors = new HashMap<>();   // taskId -> list of successor taskIds

        for (Task t : tasks) {
            taskMap.put(t.getId(), t);
            predecessors.put(t.getId(), new ArrayList<>());
            successors.put(t.getId(), new ArrayList<>());
        }

        for (Dependency dep : dependencies) {
            String from = dep.getPredecessor().getId();
            String to = dep.getSuccessor().getId();
            if (taskMap.containsKey(from) && taskMap.containsKey(to)) {
                predecessors.get(to).add(from);
                successors.get(from).add(to);
            }
        }

        // Forward pass: calculate early start/finish
        for (Task task : topologicalSort(tasks, predecessors)) {
            LocalDate earlyStart = task.getPlannedStart() != null ? task.getPlannedStart().toLocalDate() : task.getActualStart();

            // Apply constraint
            if ("MUST_START_ON".equals(task.getConstraintType()) && task.getConstraintDate() != null) {
                earlyStart = task.getConstraintDate();
            } else if ("START_NO_EARLIER_THAN".equals(task.getConstraintType()) && task.getConstraintDate() != null) {
                if (earlyStart == null || earlyStart.isBefore(task.getConstraintDate())) {
                    earlyStart = task.getConstraintDate();
                }
            }

            // Consider predecessor finish dates
            for (String predId : predecessors.get(task.getId())) {
                Task pred = taskMap.get(predId);
                if (pred.getEarlyFinish() != null) {
                    LocalDate afterPred = pred.getEarlyFinish().plusDays(1);
                    if (earlyStart == null || afterPred.isAfter(earlyStart)) {
                        earlyStart = afterPred;
                    }
                }
            }

            if (earlyStart == null) earlyStart = LocalDate.now();
            task.setEarlyStart(earlyStart);

            long duration = getDurationDays(task);
            task.setEarlyFinish(earlyStart.plusDays(Math.max(0, duration - 1)));
        }

        // Find project end date (latest early finish)
        LocalDate projectEnd = tasks.stream()
                .map(Task::getEarlyFinish)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        // Backward pass: calculate late start/finish
        List<Task> reverseOrder = topologicalSort(tasks, predecessors);
        Collections.reverse(reverseOrder);

        for (Task task : reverseOrder) {
            LocalDate lateFinish;

            if (successors.get(task.getId()).isEmpty()) {
                lateFinish = projectEnd;
            } else {
                lateFinish = null;
                for (String succId : successors.get(task.getId())) {
                    Task succ = taskMap.get(succId);
                    if (succ.getLateStart() != null) {
                        LocalDate beforeSucc = succ.getLateStart().minusDays(1);
                        if (lateFinish == null || beforeSucc.isBefore(lateFinish)) {
                            lateFinish = beforeSucc;
                        }
                    }
                }
                if (lateFinish == null) lateFinish = projectEnd;
            }

            // Apply constraint
            if ("MUST_FINISH_ON".equals(task.getConstraintType()) && task.getConstraintDate() != null) {
                lateFinish = task.getConstraintDate();
            } else if ("FINISH_NO_LATER_THAN".equals(task.getConstraintType()) && task.getConstraintDate() != null) {
                if (lateFinish.isAfter(task.getConstraintDate())) {
                    lateFinish = task.getConstraintDate();
                }
            }

            task.setLateFinish(lateFinish);

            long duration = getDurationDays(task);
            task.setLateStart(lateFinish.minusDays(Math.max(0, duration - 1)));

            // Calculate float
            if (task.getEarlyStart() != null && task.getLateStart() != null) {
                int totalFloat = (int) ChronoUnit.DAYS.between(task.getEarlyStart(), task.getLateStart());
                task.setTotalFloat(totalFloat);
                task.setCritical(totalFloat == 0);
            }
        }

        taskRepository.saveAll(tasks);

        eventJournalService.record("SCHEDULE_RECALCULATED", "PROJECT", projectId,
                "{\"tasksCount\":" + tasks.size() + "}", null);

        log.info("Recalculated schedule for project {}: {} tasks processed", projectId, tasks.size());
    }

    private long getDurationDays(Task task) {
        if (task.getPlannedStart() != null && task.getPlannedFinish() != null) {
            return ChronoUnit.DAYS.between(task.getPlannedStart().toLocalDate(), task.getPlannedFinish().toLocalDate()) + 1;
        }
        if (task.getEstimatedEffort() != null && task.getEstimatedEffort() > 0) {
            return Math.max(1, task.getEstimatedEffort() / 480); // 8h/day = 480 min
        }
        return 1; // default 1 day
    }

    private List<Task> topologicalSort(List<Task> tasks, Map<String, List<String>> predecessors) {
        Map<String, Task> taskMap = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();

        for (Task t : tasks) {
            taskMap.put(t.getId(), t);
            inDegree.put(t.getId(), predecessors.getOrDefault(t.getId(), List.of()).size());
        }

        Queue<String> queue = new LinkedList<>();
        for (var entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) queue.add(entry.getKey());
        }

        List<Task> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            String id = queue.poll();
            sorted.add(taskMap.get(id));

            for (Task t : tasks) {
                if (predecessors.get(t.getId()).contains(id)) {
                    inDegree.merge(t.getId(), -1, Integer::sum);
                    if (inDegree.get(t.getId()) == 0) {
                        queue.add(t.getId());
                    }
                }
            }
        }

        // Add any remaining (cycles) at the end
        for (Task t : tasks) {
            if (!sorted.contains(t)) sorted.add(t);
        }

        return sorted;
    }
}
