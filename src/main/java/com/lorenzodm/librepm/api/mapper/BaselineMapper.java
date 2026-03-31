package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.BaselineResponse;
import com.lorenzodm.librepm.api.dto.response.BaselineTaskSnapshotResponse;
import com.lorenzodm.librepm.core.entity.Baseline;
import com.lorenzodm.librepm.core.entity.BaselineTaskSnapshot;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BaselineMapper {

    public BaselineResponse toResponse(Baseline baseline) {
        if (baseline == null) return null;
        List<BaselineTaskSnapshotResponse> snapshots = baseline.getTaskSnapshots() != null
                ? baseline.getTaskSnapshots().stream().map(this::toSnapshotResponse).collect(Collectors.toList())
                : List.of();

        return new BaselineResponse(
                baseline.getId(),
                baseline.getProject().getId(),
                baseline.getName(),
                baseline.getSnapshotDate(),
                snapshots,
                baseline.getCreatedAt()
        );
    }

    public BaselineTaskSnapshotResponse toSnapshotResponse(BaselineTaskSnapshot snapshot) {
        return new BaselineTaskSnapshotResponse(
                snapshot.getId(),
                snapshot.getTask().getId(),
                snapshot.getTask().getTitle(),
                snapshot.getPlannedStart(),
                snapshot.getPlannedFinish(),
                snapshot.getEstimatedEffort(),
                // Current values filled in by VarianceService
                snapshot.getTask().getPlannedStart(),
                snapshot.getTask().getPlannedFinish(),
                snapshot.getTask().getEstimatedEffort(),
                computeScheduleVariance(snapshot),
                computeEffortVariance(snapshot)
        );
    }

    private Long computeScheduleVariance(BaselineTaskSnapshot snapshot) {
        if (snapshot.getPlannedFinish() == null || snapshot.getTask().getPlannedFinish() == null) return null;
        return java.time.Duration.between(snapshot.getPlannedFinish(), snapshot.getTask().getPlannedFinish()).toMinutes();
    }

    private Integer computeEffortVariance(BaselineTaskSnapshot snapshot) {
        if (snapshot.getEstimatedEffort() == null || snapshot.getTask().getEstimatedEffort() == null) return null;
        return snapshot.getTask().getEstimatedEffort() - snapshot.getEstimatedEffort();
    }
}
