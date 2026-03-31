package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.response.AnalyticsResponse;
import com.lorenzodm.librepm.api.dto.response.FocusHeatmapResponse;
import com.lorenzodm.librepm.api.dto.response.FocusStatsResponse;
import com.lorenzodm.librepm.api.dto.response.ProjectAnalyticsResponse;
import com.lorenzodm.librepm.api.dto.response.TaskDeviationResponse;
import com.lorenzodm.librepm.core.entity.FocusSession;
import com.lorenzodm.librepm.core.entity.Project;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.repository.FocusSessionRepository;
import com.lorenzodm.librepm.repository.ProjectRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.service.AnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final FocusSessionRepository focusSessionRepository;

    public AnalyticsServiceImpl(ProjectRepository projectRepository, TaskRepository taskRepository, FocusSessionRepository focusSessionRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.focusSessionRepository = focusSessionRepository;
    }

    @Override
    public AnalyticsResponse getEstimatesAnalytics(String userId, String projectId) {
        List<Project> projects;
        if (projectId != null && !projectId.isBlank()) {
            projects = projectRepository.findById(projectId)
                    .filter(p -> p.getOwner().getId().equals(userId)) // TODO: ProjectMember check
                    .map(List::of)
                    .orElse(List.of());
        } else {
            projects = projectRepository.findByOwnerId(userId);
        }

        List<Task> allTasks = new ArrayList<>();
        
        // Raccogli tutti i task rilevanti (con stima > 0)
        for (Project p : projects) {
            allTasks.addAll(taskRepository.findByProjectId(p.getId()).stream()
                    .filter(t -> t.getEstimatedEffort() != null && t.getEstimatedEffort() > 0)
                    .toList());
        }

        if (allTasks.isEmpty()) {
            return new AnalyticsResponse(0.0, List.of(), List.of(), List.of());
        }

        // Calcola deviazioni per task
        List<TaskDeviationResponse> taskDeviations = allTasks.stream()
                .map(this::calculateDeviation)
                .toList();

        // 1. Media Globale
        double globalDeviation = taskDeviations.stream()
                .mapToDouble(TaskDeviationResponse::deviationPercentage)
                .average()
                .orElse(0.0);

        // 2. Analytics per Progetto
        List<ProjectAnalyticsResponse> projectAnalytics = projects.stream()
                .map(p -> calculateProjectAnalytics(p, taskDeviations))
                .filter(pa -> pa.totalTasksWithEstimates() > 0)
                .sorted(Comparator.comparingDouble(ProjectAnalyticsResponse::deviationPercentage).reversed())
                .toList();

        // 3. Top Outliers
        List<TaskDeviationResponse> topUnderestimated = taskDeviations.stream()
                .filter(t -> t.deviationPercentage() > 0) // Ci ho messo di più
                .sorted(Comparator.comparingDouble(TaskDeviationResponse::deviationPercentage).reversed())
                .limit(5)
                .toList();

        List<TaskDeviationResponse> topOverestimated = taskDeviations.stream()
                .filter(t -> t.deviationPercentage() < 0) // Ci ho messo di meno
                .sorted(Comparator.comparingDouble(TaskDeviationResponse::deviationPercentage))
                .limit(5)
                .toList();

        return new AnalyticsResponse(
                globalDeviation,
                projectAnalytics,
                topUnderestimated,
                topOverestimated
        );
    }

    @Override
    public FocusHeatmapResponse getFocusHeatmap(String userId, String projectId, int daysRange) {
        Instant since = Instant.now().minus(daysRange, ChronoUnit.DAYS);
        
        List<FocusSession> sessions;
        if (projectId != null && !projectId.isBlank()) {
            sessions = focusSessionRepository.findByUserIdAndStartedAtAfter(userId, since).stream()
                    .filter(s -> s.getTask().getProject().getId().equals(projectId))
                    .toList();
        } else {
            sessions = focusSessionRepository.findByUserIdAndStartedAtAfter(userId, since);
        }

        // Raggruppa per data
        Map<LocalDate, List<FocusSession>> grouped = sessions.stream()
                .collect(Collectors.groupingBy(s -> s.getStartedAt().atZone(ZoneId.systemDefault()).toLocalDate()));

        List<FocusHeatmapResponse.DayEntry> entries = grouped.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<FocusSession> dailySessions = entry.getValue();
                    int sessionCount = dailySessions.size();
                    long totalMinutes = dailySessions.stream()
                            .mapToLong(s -> s.getDurationMs() / 60000)
                            .sum();
                    return new FocusHeatmapResponse.DayEntry(date, sessionCount, totalMinutes);
                })
                .sorted(Comparator.comparing(FocusHeatmapResponse.DayEntry::date))
                .toList();

        return new FocusHeatmapResponse(entries);
    }

    @Override
    public FocusStatsResponse getFocusStats(String userId, String period) {
        Instant since;
        switch (period.toLowerCase()) {
            case "day":
                since = Instant.now().minus(1, ChronoUnit.DAYS);
                break;
            case "month":
                since = Instant.now().minus(30, ChronoUnit.DAYS);
                break;
            case "year":
                since = Instant.now().minus(365, ChronoUnit.DAYS);
                break;
            case "week":
            default:
                since = Instant.now().minus(7, ChronoUnit.DAYS);
                break;
        }

        List<FocusSession> sessions = focusSessionRepository.findByUserIdAndStartedAtAfter(userId, since);

        long totalMinutes = sessions.stream()
                .mapToLong(s -> s.getDurationMs() / 60000)
                .sum();

        return new FocusStatsResponse(totalMinutes, sessions.size(), period);
    }

    private TaskDeviationResponse calculateDeviation(Task t) {
        int estimated = t.getEstimatedEffort();
        int actual = t.getActualEffort() != null ? t.getActualEffort() : 0;
        
        // Formula: (actual - estimated) / estimated * 100
        // Se actual > estimated -> positivo (sottostimato, ci ho messo di più)
        // Se actual < estimated -> negativo (sovrastimato, ci ho messo di meno)
        double deviation = ((double) (actual - estimated) / estimated) * 100.0;

        return new TaskDeviationResponse(
                t.getId(),
                t.getTitle(),
                t.getProject().getId(),
                t.getProject().getName(),
                estimated,
                actual,
                deviation
        );
    }

    private ProjectAnalyticsResponse calculateProjectAnalytics(Project p, List<TaskDeviationResponse> allDeviations) {
        List<TaskDeviationResponse> projectTasks = allDeviations.stream()
                .filter(t -> t.projectId().equals(p.getId()))
                .toList();

        if (projectTasks.isEmpty()) {
            return new ProjectAnalyticsResponse(p.getId(), p.getName(), 0.0, 0, 0, 0);
        }

        double avgDeviation = projectTasks.stream()
                .mapToDouble(TaskDeviationResponse::deviationPercentage)
                .average()
                .orElse(0.0);

        int totalEstimated = projectTasks.stream().mapToInt(TaskDeviationResponse::estimatedMinutes).sum();
        int totalActual = projectTasks.stream().mapToInt(TaskDeviationResponse::actualMinutes).sum();

        return new ProjectAnalyticsResponse(
                p.getId(),
                p.getName(),
                avgDeviation,
                projectTasks.size(),
                totalEstimated,
                totalActual
        );
    }
}
