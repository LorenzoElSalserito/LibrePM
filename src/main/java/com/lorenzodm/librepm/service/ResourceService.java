package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.response.ResourceAllocationResponse;

import java.time.LocalDate;

public interface ResourceService {
    ResourceAllocationResponse getResourceAllocation(String projectId, LocalDate startDate, LocalDate endDate);
}
