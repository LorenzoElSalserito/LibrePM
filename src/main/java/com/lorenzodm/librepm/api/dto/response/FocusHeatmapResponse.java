package com.lorenzodm.librepm.api.dto.response;

import java.time.LocalDate;
import java.util.List;

public record FocusHeatmapResponse(
        List<DayEntry> data
) {
    public record DayEntry(
            LocalDate date,
            int sessionCount,
            long totalMinutes // Minuti totali
    ) {}
}
