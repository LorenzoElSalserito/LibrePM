package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.response.BaselineTaskSnapshotResponse;
import com.lorenzodm.librepm.api.dto.response.VarianceResponse;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.api.mapper.BaselineMapper;
import com.lorenzodm.librepm.core.entity.Baseline;
import com.lorenzodm.librepm.core.entity.BaselineTaskSnapshot;
import com.lorenzodm.librepm.repository.BaselineRepository;
import com.lorenzodm.librepm.service.VarianceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PRD-11 Variance calculation service.
 * PRD-11-BR-004: distinguishes schedule variance and effort variance.
 */
@Service
@Transactional(readOnly = true)
public class VarianceServiceImpl implements VarianceService {

    private final BaselineRepository baselineRepository;
    private final BaselineMapper baselineMapper;

    public VarianceServiceImpl(BaselineRepository baselineRepository, BaselineMapper baselineMapper) {
        this.baselineRepository = baselineRepository;
        this.baselineMapper = baselineMapper;
    }

    @Override
    public VarianceResponse calculateVariance(String userId, String projectId, String baselineId) {
        Baseline baseline = baselineRepository.findById(baselineId)
                .filter(b -> b.getProject().getId().equals(projectId))
                .orElseThrow(() -> new ResourceNotFoundException("Baseline non trovata"));

        return buildVarianceResponse(baseline);
    }

    @Override
    public VarianceResponse calculateLatestVariance(String userId, String projectId) {
        List<Baseline> baselines = baselineRepository.findByProjectIdOrderBySnapshotDateDesc(projectId);
        if (baselines.isEmpty()) {
            return null;
        }
        return buildVarianceResponse(baselines.get(0));
    }

    private VarianceResponse buildVarianceResponse(Baseline baseline) {
        List<BaselineTaskSnapshot> snapshots = baseline.getTaskSnapshots();

        // Compute baseline project end (latest plannedFinish in snapshot)
        LocalDateTime baselineProjectEnd = snapshots.stream()
                .filter(s -> s.getPlannedFinish() != null)
                .map(BaselineTaskSnapshot::getPlannedFinish)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // Compute current project end
        LocalDateTime currentProjectEnd = snapshots.stream()
                .filter(s -> s.getTask().getPlannedFinish() != null)
                .map(s -> s.getTask().getPlannedFinish())
                .max(LocalDateTime::compareTo)
                .orElse(null);

        long scheduleVarianceDays = 0;
        if (baselineProjectEnd != null && currentProjectEnd != null) {
            scheduleVarianceDays = ChronoUnit.DAYS.between(baselineProjectEnd, currentProjectEnd);
        }

        // Effort variance
        int baselineEffort = snapshots.stream()
                .mapToInt(s -> s.getEstimatedEffort() != null ? s.getEstimatedEffort() : 0)
                .sum();
        int currentEffort = snapshots.stream()
                .mapToInt(s -> s.getTask().getEstimatedEffort() != null ? s.getTask().getEstimatedEffort() : 0)
                .sum();
        int effortVariance = currentEffort - baselineEffort;

        // PRD-11: project status
        String status;
        if (scheduleVarianceDays <= 0 && effortVariance <= 0) {
            status = "ON_TRACK";
        } else if (scheduleVarianceDays <= 3 || effortVariance <= baselineEffort * 0.1) {
            status = "AT_RISK";
        } else {
            status = "DELAYED";
        }

        List<BaselineTaskSnapshotResponse> taskVariances = snapshots.stream()
                .map(baselineMapper::toSnapshotResponse)
                .collect(Collectors.toList());

        return new VarianceResponse(
                baseline.getId(),
                baseline.getName(),
                baseline.getProject().getId(),
                baselineProjectEnd,
                currentProjectEnd,
                scheduleVarianceDays,
                baselineEffort,
                currentEffort,
                effortVariance,
                taskVariances,
                status
        );
    }
}
