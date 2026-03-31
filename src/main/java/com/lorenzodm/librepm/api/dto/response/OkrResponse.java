package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;
import java.util.List;

public record OkrResponse(
        String id,
        String projectId,
        String objective,
        List<SuccessMetricResponse> keyResults,
        double overallProgress, // avg of key result achievement %
        Instant createdAt,
        Instant updatedAt
) {}
