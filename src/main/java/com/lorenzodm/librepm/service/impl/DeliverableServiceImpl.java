package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateDeliverableRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateDeliverableRequest;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.Deliverable;
import com.lorenzodm.librepm.core.entity.Project;
import com.lorenzodm.librepm.repository.DeliverableRepository;
import com.lorenzodm.librepm.repository.ProjectRepository;
import com.lorenzodm.librepm.service.DeliverableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DeliverableServiceImpl implements DeliverableService {

    private final DeliverableRepository deliverableRepository;
    private final ProjectRepository projectRepository;

    public DeliverableServiceImpl(DeliverableRepository deliverableRepository,
                                  ProjectRepository projectRepository) {
        this.deliverableRepository = deliverableRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public Deliverable create(String userId, String projectId, CreateDeliverableRequest req) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Progetto non trovato"));

        Deliverable deliverable = new Deliverable();
        deliverable.setProject(project);
        deliverable.setName(req.name());
        deliverable.setDescription(req.description());
        deliverable.setDueDate(req.dueDate());
        deliverable.setProgress(req.progress() != null ? req.progress() : 0);
        if (req.riskStatus() != null) {
            deliverable.setRiskStatus(Deliverable.RiskStatus.valueOf(req.riskStatus()));
        }
        return deliverableRepository.save(deliverable);
    }

    @Override
    @Transactional(readOnly = true)
    public Deliverable getById(String userId, String projectId, String deliverableId) {
        return deliverableRepository.findById(deliverableId)
                .filter(d -> d.getProject().getId().equals(projectId))
                .orElseThrow(() -> new ResourceNotFoundException("Deliverable non trovato"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Deliverable> listByProject(String userId, String projectId) {
        return deliverableRepository.findByProjectId(projectId);
    }

    @Override
    public Deliverable update(String userId, String projectId, String deliverableId, UpdateDeliverableRequest req) {
        Deliverable deliverable = getById(userId, projectId, deliverableId);
        if (req.name() != null) deliverable.setName(req.name());
        if (req.description() != null) deliverable.setDescription(req.description());
        if (req.dueDate() != null) deliverable.setDueDate(req.dueDate());
        if (req.riskStatus() != null) deliverable.setRiskStatus(Deliverable.RiskStatus.valueOf(req.riskStatus()));
        if (req.progress() != null) {
            deliverable.setProgress(req.progress());
            if (req.progress() == 100 && deliverable.getCompletedAt() == null) {
                deliverable.setCompletedAt(LocalDateTime.now());
            } else if (req.progress() < 100) {
                deliverable.setCompletedAt(null);
            }
        }
        return deliverableRepository.save(deliverable);
    }

    @Override
    public void delete(String userId, String projectId, String deliverableId) {
        Deliverable deliverable = getById(userId, projectId, deliverableId);
        deliverableRepository.delete(deliverable);
    }
}
