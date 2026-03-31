package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.OkrResponse;
import com.lorenzodm.librepm.api.dto.response.SuccessMetricResponse;
import com.lorenzodm.librepm.api.dto.response.TargetAchievedRecordResponse;
import com.lorenzodm.librepm.core.entity.Okr;
import com.lorenzodm.librepm.core.entity.SuccessMetric;
import com.lorenzodm.librepm.core.entity.TargetAchievedRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OkrMapper {

    public OkrResponse toResponse(Okr okr) {
        if (okr == null) return null;
        List<SuccessMetricResponse> keyResults = okr.getKeyResults() != null
                ? okr.getKeyResults().stream().map(this::toMetricResponse).collect(Collectors.toList())
                : List.of();

        double overallProgress = keyResults.stream()
                .mapToDouble(SuccessMetricResponse::achievementPercentage)
                .average()
                .orElse(0.0);

        return new OkrResponse(
                okr.getId(),
                okr.getProject().getId(),
                okr.getObjective(),
                keyResults,
                overallProgress,
                okr.getCreatedAt(),
                okr.getUpdatedAt()
        );
    }

    public SuccessMetricResponse toMetricResponse(SuccessMetric metric) {
        double achievement = metric.getTargetValue() > 0
                ? (metric.getCurrentValue() / metric.getTargetValue()) * 100.0
                : 0.0;

        return new SuccessMetricResponse(
                metric.getId(),
                metric.getOkr().getId(),
                metric.getName(),
                metric.getTargetValue(),
                metric.getCurrentValue(),
                metric.getUnit(),
                achievement,
                List.of(), // records loaded separately when needed
                metric.getCreatedAt(),
                metric.getUpdatedAt()
        );
    }

    public TargetAchievedRecordResponse toRecordResponse(TargetAchievedRecord record) {
        return new TargetAchievedRecordResponse(
                record.getId(),
                record.getMetric().getId(),
                record.getAchievedValue(),
                record.getRecordDate(),
                record.getNote(),
                record.getCreatedAt()
        );
    }
}
