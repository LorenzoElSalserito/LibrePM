package com.lorenzodm.librepm.api.dto.response;

public record FocusStatsResponse(
        long totalMinutes,
        int sessionsCount,
        String period
) {}
