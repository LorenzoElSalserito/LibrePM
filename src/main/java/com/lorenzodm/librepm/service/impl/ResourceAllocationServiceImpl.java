package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateResourceAllocationRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateResourceAllocationRequest;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.Project;
import com.lorenzodm.librepm.core.entity.ResourceAllocation;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.ProjectRepository;
import com.lorenzodm.librepm.repository.ResourceAllocationRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.ResourceAllocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ResourceAllocationServiceImpl implements ResourceAllocationService {

    private final ResourceAllocationRepository allocationRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public ResourceAllocationServiceImpl(ResourceAllocationRepository allocationRepository,
                                         UserRepository userRepository,
                                         ProjectRepository projectRepository) {
        this.allocationRepository = allocationRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public ResourceAllocation create(CreateResourceAllocationRequest req) {
        User user = userRepository.findById(req.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        ResourceAllocation allocation = new ResourceAllocation();
        allocation.setUser(user);
        allocation.setStartDate(req.startDate());
        allocation.setEndDate(req.endDate());
        allocation.setPercentage(req.percentage());

        if (req.projectId() != null && !req.projectId().isBlank()) {
            Project project = projectRepository.findById(req.projectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Progetto non trovato"));
            allocation.setProject(project);
        }

        return allocationRepository.save(allocation);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceAllocation getById(String allocationId) {
        return allocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Allocazione non trovata"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceAllocation> listByUser(String userId) {
        return allocationRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceAllocation> listByProject(String projectId) {
        return allocationRepository.findByProjectId(projectId);
    }

    @Override
    public ResourceAllocation update(String allocationId, UpdateResourceAllocationRequest req) {
        ResourceAllocation allocation = getById(allocationId);
        if (req.startDate() != null) allocation.setStartDate(req.startDate());
        if (req.endDate() != null) allocation.setEndDate(req.endDate());
        if (req.percentage() != null) allocation.setPercentage(req.percentage());
        return allocationRepository.save(allocation);
    }

    @Override
    public void delete(String allocationId) {
        ResourceAllocation allocation = getById(allocationId);
        allocationRepository.delete(allocation);
    }
}
