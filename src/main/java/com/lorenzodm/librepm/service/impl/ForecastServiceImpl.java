package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.core.entity.ResourceAllocation;
import com.lorenzodm.librepm.core.entity.Task;

import com.lorenzodm.librepm.repository.ResourceAllocationRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.repository.TimeEntryRepository;
import com.lorenzodm.librepm.service.CapacityService;
import com.lorenzodm.librepm.service.ForecastService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ForecastServiceImpl implements ForecastService {

    private final TaskRepository taskRepo;
    private final TimeEntryRepository timeEntryRepo;
    private final ResourceAllocationRepository allocationRepo;
    private final CapacityService capacityService;

    public ForecastServiceImpl(TaskRepository taskRepo,
                               TimeEntryRepository timeEntryRepo,
                               ResourceAllocationRepository allocationRepo,
                               CapacityService capacityService) {
        this.taskRepo = taskRepo;
        this.timeEntryRepo = timeEntryRepo;
        this.allocationRepo = allocationRepo;
        this.capacityService = capacityService;
    }

    @Override
    public Map<String, Object> computeEarnedValueMetrics(String projectId, LocalDate asOfDate) {
        List<Task> tasks = taskRepo.findByProjectId(projectId);
        Map<String, Object> metrics = new LinkedHashMap<>();

        if (tasks.isEmpty()) {
            metrics.put("spi", 0.0);
            metrics.put("cpi", 0.0);
            metrics.put("eac", 0.0);
            return metrics;
        }

        double totalPlannedEffort = 0;
        double earnedEffort = 0;
        double actualEffort = 0;

        for (Task task : tasks) {
            double planned = task.getEstimatedEffortHours() != null ? task.getEstimatedEffortHours() : 0;
            totalPlannedEffort += planned;

            // Earned = planned * (actual/planned ratio, capped at 1.0)
            double actual = task.getActualEffortHours() != null ? task.getActualEffortHours() : 0;
            double completionRatio = planned > 0 ? Math.min(actual / planned, 1.0) : 0;
            earnedEffort += planned * completionRatio;
        }

        // Sum actual hours from time entries (durationMinutes → hours)
        int totalMinutes = timeEntryRepo.sumDurationByProjectId(projectId);
        actualEffort = totalMinutes / 60.0;

        // Budget At Completion
        double bac = totalPlannedEffort;

        // Schedule Performance Index
        double spi = totalPlannedEffort > 0 ? earnedEffort / totalPlannedEffort : 0;

        // Cost Performance Index
        double cpi = actualEffort > 0 ? earnedEffort / actualEffort : 0;

        // Estimate At Completion
        double eac = cpi > 0 ? bac / cpi : bac;

        // Estimate To Complete
        double etc = Math.max(0, eac - actualEffort);

        // Variance At Completion
        double vac = bac - eac;

        metrics.put("bac", Math.round(bac * 100.0) / 100.0);
        metrics.put("earnedValue", Math.round(earnedEffort * 100.0) / 100.0);
        metrics.put("actualCost", Math.round(actualEffort * 100.0) / 100.0);
        metrics.put("spi", Math.round(spi * 100.0) / 100.0);
        metrics.put("cpi", Math.round(cpi * 100.0) / 100.0);
        metrics.put("eac", Math.round(eac * 100.0) / 100.0);
        metrics.put("etc", Math.round(etc * 100.0) / 100.0);
        metrics.put("vac", Math.round(vac * 100.0) / 100.0);

        return metrics;
    }

    @Override
    public Map<String, Object> detectResourceIssues(String projectId, LocalDate from, LocalDate to) {
        List<ResourceAllocation> allocations = allocationRepo.findByProjectIdAndPeriodOverlap(projectId, from, to);

        // Group allocations by userId
        Map<String, List<ResourceAllocation>> byUser = allocations.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getId()));

        List<Map<String, Object>> overbooked = new ArrayList<>();
        List<Map<String, Object>> underutilized = new ArrayList<>();

        for (var entry : byUser.entrySet()) {
            String userId = entry.getKey();
            List<ResourceAllocation> userAllocations = entry.getValue();

            // Check each day in the range
            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                double capacity = capacityService.getAvailableHours(userId, date);
                if (capacity <= 0) continue;

                // Sum allocation percentages for this day
                final LocalDate checkDate = date;
                int totalPct = userAllocations.stream()
                        .filter(a -> !a.getStartDate().isAfter(checkDate) && !a.getEndDate().isBefore(checkDate))
                        .mapToInt(ResourceAllocation::getPercentage)
                        .sum();

                if (totalPct > 100) {
                    Map<String, Object> issue = new LinkedHashMap<>();
                    issue.put("userId", userId);
                    issue.put("date", date.toString());
                    issue.put("allocationPct", totalPct);
                    issue.put("type", "OVERBOOKED");
                    overbooked.add(issue);
                } else if (totalPct < 50 && totalPct > 0) {
                    Map<String, Object> issue = new LinkedHashMap<>();
                    issue.put("userId", userId);
                    issue.put("date", date.toString());
                    issue.put("allocationPct", totalPct);
                    issue.put("type", "UNDERUTILIZED");
                    underutilized.add(issue);
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overbooked", overbooked);
        result.put("underutilized", underutilized);
        result.put("overbookedCount", overbooked.size());
        result.put("underutilizedCount", underutilized.size());
        return result;
    }
}
