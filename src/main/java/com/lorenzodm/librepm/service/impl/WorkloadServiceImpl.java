package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.response.WorkloadSliceResponse;
import com.lorenzodm.librepm.core.entity.Assignment;
import com.lorenzodm.librepm.core.entity.ResourceAllocation;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.AssignmentRepository;
import com.lorenzodm.librepm.repository.ProjectMemberRepository;
import com.lorenzodm.librepm.repository.ResourceAllocationRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.CapacityEngine;
import com.lorenzodm.librepm.service.WorkloadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PRD-12 Workload service.
 * Calculates capacity vs workload, detects overbooking (PRD-12-FR-007).
 */
@Service
@Transactional(readOnly = true)
public class WorkloadServiceImpl implements WorkloadService {

    private static final int DEFAULT_DAILY_MINUTES = 480; // 8h

    private final ResourceAllocationRepository allocationRepository;
    private final AssignmentRepository assignmentRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final CapacityEngine capacityEngine;

    public WorkloadServiceImpl(ResourceAllocationRepository allocationRepository,
                               AssignmentRepository assignmentRepository,
                               ProjectMemberRepository projectMemberRepository,
                               UserRepository userRepository,
                               CapacityEngine capacityEngine) {
        this.allocationRepository = allocationRepository;
        this.assignmentRepository = assignmentRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.capacityEngine = capacityEngine;
    }

    @Override
    public List<WorkloadSliceResponse> getProjectWorkload(String userId, String projectId,
                                                           LocalDate periodStart, LocalDate periodEnd) {
        List<String> memberIds = projectMemberRepository.findByProjectId(projectId)
                .stream().map(m -> m.getUser().getId()).collect(Collectors.toList());

        List<WorkloadSliceResponse> result = new ArrayList<>();
        for (String memberId : memberIds) {
            User member = userRepository.findById(memberId).orElse(null);
            if (member == null) continue;
            result.add(buildSlice(member, projectId, periodStart, periodEnd));
        }
        return result;
    }

    @Override
    public WorkloadSliceResponse getUserWorkload(String userId, LocalDate periodStart, LocalDate periodEnd) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.lorenzodm.librepm.api.exception.ResourceNotFoundException("Utente non trovato"));
        return buildSlice(user, null, periodStart, periodEnd);
    }

    private WorkloadSliceResponse buildSlice(User user, String projectId,
                                              LocalDate periodStart, LocalDate periodEnd) {
        // Available minutes from capacity engine (uses default calendar)
        int availableMinutes = capacityEngine.getWorkingMinutes(periodStart, periodEnd);

        // Allocated minutes: percentage of available time from resource allocations
        List<ResourceAllocation> allocations = allocationRepository
                .findByUserIdAndPeriodOverlap(user.getId(), periodStart, periodEnd);
        int allocatedMinutes = allocations.stream()
                .mapToInt(a -> (int) (availableMinutes * a.getPercentage() / 100.0))
                .sum();

        // Assigned effort: sum of estimatedEffort from assignments in this period
        List<Assignment> assignments = assignmentRepository.findByUserId(user.getId());
        List<Assignment> periodAssignments = assignments.stream()
                .filter(a -> isTaskInPeriod(a.getTask(), periodStart, periodEnd))
                .filter(a -> projectId == null || a.getTask().getProject().getId().equals(projectId))
                .collect(Collectors.toList());

        int assignedEffort = periodAssignments.stream()
                .mapToInt(a -> a.getEstimatedEffort() != null ? a.getEstimatedEffort()
                        : (a.getTask().getEstimatedEffort() != null ? a.getTask().getEstimatedEffort() : 0))
                .sum();

        boolean isOverbooked = assignedEffort > availableMinutes; // PRD-12-FR-007

        List<WorkloadSliceResponse.TaskSlice> taskSlices = periodAssignments.stream()
                .map(a -> new WorkloadSliceResponse.TaskSlice(
                        a.getTask().getId(),
                        a.getTask().getTitle(),
                        a.getTask().getStatus() != null ? a.getTask().getStatus().getName() : null,
                        a.getEstimatedEffort() != null ? a.getEstimatedEffort() : a.getTask().getEstimatedEffort(),
                        a.getTask().getActualEffort()
                ))
                .collect(Collectors.toList());

        return new WorkloadSliceResponse(
                user.getId(),
                user.getDisplayName(),
                periodStart,
                periodEnd,
                availableMinutes,
                allocatedMinutes,
                assignedEffort,
                isOverbooked,
                taskSlices
        );
    }

    private boolean isTaskInPeriod(Task task, LocalDate periodStart, LocalDate periodEnd) {
        if (task.getPlannedStart() != null && task.getPlannedFinish() != null) {
            LocalDate taskStart = task.getPlannedStart().toLocalDate();
            LocalDate taskEnd = task.getPlannedFinish().toLocalDate();
            return !taskEnd.isBefore(periodStart) && !taskStart.isAfter(periodEnd);
        }
        if (task.getDeadline() != null) {
            return !task.getDeadline().isBefore(periodStart) && !task.getDeadline().isAfter(periodEnd);
        }
        return true; // include unscheduled tasks
    }
}
