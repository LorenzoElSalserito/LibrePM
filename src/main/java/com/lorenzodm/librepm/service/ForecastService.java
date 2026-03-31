package com.lorenzodm.librepm.service;

import java.time.LocalDate;
import java.util.Map;

public interface ForecastService {

    /**
     * Computes forecast metrics for a project: SPI, CPI, EAC, ETC, VAC.
     */
    Map<String, Object> computeEarnedValueMetrics(String projectId, LocalDate asOfDate);

    /**
     * Detects overbooking (allocation > capacity) and underutilization.
     */
    Map<String, Object> detectResourceIssues(String projectId, LocalDate from, LocalDate to);
}
