package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.CreateResourceAllocationRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateResourceAllocationRequest;
import com.lorenzodm.librepm.core.entity.ResourceAllocation;

import java.util.List;

public interface ResourceAllocationService {

    ResourceAllocation create(CreateResourceAllocationRequest req);

    ResourceAllocation getById(String allocationId);

    List<ResourceAllocation> listByUser(String userId);

    List<ResourceAllocation> listByProject(String projectId);

    ResourceAllocation update(String allocationId, UpdateResourceAllocationRequest req);

    void delete(String allocationId);
}
