package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.Phase;
import com.lorenzodm.librepm.repository.PhaseRepository;
import com.lorenzodm.librepm.repository.ProjectRepository;
import com.lorenzodm.librepm.service.PhaseService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PhaseServiceImpl implements PhaseService {

    private final PhaseRepository phaseRepository;
    private final ProjectRepository projectRepository;

    public PhaseServiceImpl(PhaseRepository phaseRepository, ProjectRepository projectRepository) {
        this.phaseRepository = phaseRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public Phase create(String projectId, String name, String description,
                        LocalDate plannedStart, LocalDate plannedEnd, String color, int sortOrder) {
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        Phase phase = new Phase();
        phase.setProject(project);
        phase.setName(name);
        phase.setDescription(description);
        phase.setPlannedStart(plannedStart);
        phase.setPlannedEnd(plannedEnd);
        phase.setColor(color);
        phase.setSortOrder(sortOrder);
        return phaseRepository.save(phase);
    }

    @Override
    public Phase update(String phaseId, String name, String description,
                        LocalDate plannedStart, LocalDate plannedEnd,
                        LocalDate actualStart, LocalDate actualEnd,
                        String status, String color, int sortOrder) {
        Phase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Phase not found: " + phaseId));

        if (name != null) phase.setName(name);
        if (description != null) phase.setDescription(description);
        if (plannedStart != null) phase.setPlannedStart(plannedStart);
        if (plannedEnd != null) phase.setPlannedEnd(plannedEnd);
        if (actualStart != null) phase.setActualStart(actualStart);
        if (actualEnd != null) phase.setActualEnd(actualEnd);
        if (status != null) phase.setStatus(Phase.Status.valueOf(status));
        if (color != null) phase.setColor(color);
        phase.setSortOrder(sortOrder);

        return phaseRepository.save(phase);
    }

    @Override
    public List<Phase> listByProject(String projectId) {
        return phaseRepository.findByProjectIdOrderBySortOrderAsc(projectId);
    }

    @Override
    public void delete(String phaseId) {
        phaseRepository.deleteById(phaseId);
    }
}
