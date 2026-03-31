package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateRiskRegisterEntryRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateRiskRegisterEntryRequest;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.Project;
import com.lorenzodm.librepm.core.entity.RiskRegisterEntry;
import com.lorenzodm.librepm.repository.ProjectRepository;
import com.lorenzodm.librepm.repository.RiskRegisterEntryRepository;
import com.lorenzodm.librepm.service.RiskRegisterEntryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RiskRegisterEntryServiceImpl implements RiskRegisterEntryService {

    private final RiskRegisterEntryRepository riskRepository;
    private final ProjectRepository projectRepository;

    public RiskRegisterEntryServiceImpl(RiskRegisterEntryRepository riskRepository,
                                        ProjectRepository projectRepository) {
        this.riskRepository = riskRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public RiskRegisterEntry create(String userId, String projectId, CreateRiskRegisterEntryRequest req) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Progetto non trovato"));

        RiskRegisterEntry entry = new RiskRegisterEntry();
        entry.setProject(project);
        entry.setDescription(req.description());
        entry.setProbability(RiskRegisterEntry.RiskLevel.valueOf(req.probability()));
        entry.setImpact(RiskRegisterEntry.RiskLevel.valueOf(req.impact()));
        entry.setMitigationStrategy(req.mitigationStrategy());
        return riskRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public RiskRegisterEntry getById(String userId, String projectId, String entryId) {
        return riskRepository.findById(entryId)
                .filter(e -> e.getProject().getId().equals(projectId))
                .orElseThrow(() -> new ResourceNotFoundException("Risk entry non trovata"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiskRegisterEntry> listByProject(String userId, String projectId) {
        return riskRepository.findByProjectId(projectId);
    }

    @Override
    public RiskRegisterEntry update(String userId, String projectId, String entryId, UpdateRiskRegisterEntryRequest req) {
        RiskRegisterEntry entry = getById(userId, projectId, entryId);
        if (req.description() != null) entry.setDescription(req.description());
        if (req.probability() != null) entry.setProbability(RiskRegisterEntry.RiskLevel.valueOf(req.probability()));
        if (req.impact() != null) entry.setImpact(RiskRegisterEntry.RiskLevel.valueOf(req.impact()));
        if (req.mitigationStrategy() != null) entry.setMitigationStrategy(req.mitigationStrategy());
        return riskRepository.save(entry);
    }

    @Override
    public void delete(String userId, String projectId, String entryId) {
        RiskRegisterEntry entry = getById(userId, projectId, entryId);
        riskRepository.delete(entry);
    }
}
