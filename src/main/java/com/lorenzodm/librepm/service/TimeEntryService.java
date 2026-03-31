package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.CreateTimeEntryRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTimeEntryRequest;
import com.lorenzodm.librepm.api.dto.response.EffortDeviationResponse;
import com.lorenzodm.librepm.core.entity.TimeEntry;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeEntryService {
    TimeEntry create(String userId, CreateTimeEntryRequest request);
    TimeEntry getById(String id);
    TimeEntry update(String id, UpdateTimeEntryRequest request);
    void delete(String id);
    List<TimeEntry> findByTaskId(String taskId);
    List<TimeEntry> findByUserId(String userId);
    List<TimeEntry> findByUserIdAndDateRange(String userId, LocalDateTime start, LocalDateTime end);
    List<TimeEntry> findByProjectId(String projectId);
    int getTotalMinutesByTaskId(String taskId);
    int getTotalMinutesByProjectId(String projectId);
    EffortDeviationResponse calculateDeviation(String taskId);
    List<EffortDeviationResponse> calculateProjectVariance(String projectId);
}
