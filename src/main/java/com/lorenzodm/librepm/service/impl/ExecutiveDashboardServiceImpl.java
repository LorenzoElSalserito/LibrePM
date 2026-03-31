package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.response.*;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.api.mapper.*;
import com.lorenzodm.librepm.core.entity.Deliverable;
import com.lorenzodm.librepm.core.entity.Project;
import com.lorenzodm.librepm.core.entity.RiskRegisterEntry;
import com.lorenzodm.librepm.repository.ProjectCharterRepository;
import com.lorenzodm.librepm.repository.ProjectRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Executive dashboard aggregation (PRD-17).
 * Combines project charter, deliverables, risks, OKRs, variance, and workload.
 */
@Service
@Transactional(readOnly = true)
public class ExecutiveDashboardServiceImpl implements ExecutiveDashboardService {

    private final ProjectRepository projectRepository;
    private final ProjectCharterRepository charterRepository;
    private final DeliverableService deliverableService;
    private final RiskRegisterEntryService riskService;
    private final OkrService okrService;
    private final VarianceService varianceService;
    private final WorkloadService workloadService;
    private final TaskRepository taskRepository;

    private final ProjectCharterMapper charterMapper;
    private final DeliverableMapper deliverableMapper;
    private final RiskRegisterEntryMapper riskMapper;
    private final OkrMapper okrMapper;

    public ExecutiveDashboardServiceImpl(
            ProjectRepository projectRepository,
            ProjectCharterRepository charterRepository,
            DeliverableService deliverableService,
            RiskRegisterEntryService riskService,
            OkrService okrService,
            VarianceService varianceService,
            WorkloadService workloadService,
            TaskRepository taskRepository,
            ProjectCharterMapper charterMapper,
            DeliverableMapper deliverableMapper,
            RiskRegisterEntryMapper riskMapper,
            OkrMapper okrMapper) {
        this.projectRepository = projectRepository;
        this.charterRepository = charterRepository;
        this.deliverableService = deliverableService;
        this.riskService = riskService;
        this.okrService = okrService;
        this.varianceService = varianceService;
        this.workloadService = workloadService;
        this.taskRepository = taskRepository;
        this.charterMapper = charterMapper;
        this.deliverableMapper = deliverableMapper;
        this.riskMapper = riskMapper;
        this.okrMapper = okrMapper;
    }

    @Override
    public ExecutiveDashboardResponse getDashboard(String userId, String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Progetto non trovato"));

        // Charter (optional)
        ProjectCharterResponse charter = charterRepository.findByProjectId(projectId)
                .map(charterMapper::toResponse)
                .orElse(null);

        // Deliverables
        List<Deliverable> deliverables = deliverableService.listByProject(userId, projectId);
        List<DeliverableResponse> deliverableResponses = deliverables.stream()
                .map(deliverableMapper::toResponse).toList();
        int completedDeliverables = (int) deliverables.stream()
                .filter(d -> d.getProgress() >= 100)
                .count();

        // Risks
        List<RiskRegisterEntry> risks = riskService.listByProject(userId, projectId);
        List<RiskRegisterEntryResponse> riskResponses = risks.stream()
                .map(riskMapper::toResponse).toList();
        int highImpactRisks = (int) risks.stream()
                .filter(r -> r.getImpact() == RiskRegisterEntry.RiskLevel.HIGH
                          || r.getImpact() == RiskRegisterEntry.RiskLevel.CRITICAL)
                .count();

        // OKRs
        List<OkrResponse> okrResponses = okrService.listByProject(userId, projectId)
                .stream().map(okrMapper::toResponse).toList();

        // Latest variance (optional — may have no baselines)
        VarianceResponse latestVariance = null;
        try {
            latestVariance = varianceService.calculateLatestVariance(userId, projectId);
        } catch (Exception ignored) {
            // No baselines yet
        }

        // Project status — derive from variance or deliverable risk
        String projectStatus = deriveProjectStatus(latestVariance, risks);

        // Workload — check overbooking for the current month
        LocalDate today = LocalDate.now();
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
        List<WorkloadSliceResponse> workloadSlices;
        int overbookedUsersCount = 0;
        try {
            workloadSlices = workloadService.getProjectWorkload(userId, projectId, today, monthEnd);
            overbookedUsersCount = (int) workloadSlices.stream()
                    .filter(WorkloadSliceResponse::isOverbooked)
                    .count();
        } catch (Exception ignored) {
            // No members or allocations yet
        }

        // Task metrics
        Double completionPct = taskRepository.getCompletionPercentage(projectId);
        double completionPercentage = completionPct != null ? completionPct : 0.0;
        long overdueTaskCount = taskRepository.countOverdueTasks(projectId, today);

        return new ExecutiveDashboardResponse(
                projectId,
                project.getName(),
                projectStatus,
                charter,
                deliverableResponses,
                completedDeliverables,
                deliverables.size(),
                riskResponses,
                highImpactRisks,
                okrResponses,
                latestVariance,
                overbookedUsersCount,
                completionPercentage,
                overdueTaskCount
        );
    }

    private String deriveProjectStatus(VarianceResponse variance,
                                       List<RiskRegisterEntry> risks) {
        // If any risk is CRITICAL impact → BLOCKED
        boolean hasCriticalRisk = risks.stream()
                .anyMatch(r -> r.getImpact() == RiskRegisterEntry.RiskLevel.CRITICAL);
        if (hasCriticalRisk) return "BLOCKED";

        if (variance == null) return "ON_TRACK";

        return switch (variance.projectStatus()) {
            case "DELAYED" -> "DELAYED";
            case "AT_RISK" -> "AT_RISK";
            default -> "ON_TRACK";
        };
    }
}
