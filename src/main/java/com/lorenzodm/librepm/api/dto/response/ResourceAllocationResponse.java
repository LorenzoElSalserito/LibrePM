package com.lorenzodm.librepm.api.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ResourceAllocationResponse(
        List<UserAllocation> allocations
) {
    public record UserAllocation(
            String userId,
            String userName,
            Map<LocalDate, Integer> dailyMinutes,     // Data -> Minuti stimati
            int totalEstimatedMinutes,                 // Totale effort stimato
            int totalActualMinutes                     // Totale effort effettivo (da focus sessions)
    ) {}
}
