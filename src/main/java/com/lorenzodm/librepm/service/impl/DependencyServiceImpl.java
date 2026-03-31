package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateDependencyRequest;
import com.lorenzodm.librepm.api.exception.ConflictException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.Dependency;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.repository.DependencyRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.service.DependencyService;
import com.lorenzodm.librepm.service.PlanningEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DependencyServiceImpl implements DependencyService {

    private final DependencyRepository dependencyRepository;
    private final TaskRepository taskRepository;
    private final PlanningEngine planningEngine;

    public DependencyServiceImpl(DependencyRepository dependencyRepository,
                                 TaskRepository taskRepository,
                                 PlanningEngine planningEngine) {
        this.dependencyRepository = dependencyRepository;
        this.taskRepository = taskRepository;
        this.planningEngine = planningEngine;
    }

    @Override
    public Dependency create(String userId, String projectId, CreateDependencyRequest req) {
        Task predecessor = taskRepository.findByIdAndProjectId(req.predecessorId(), projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Predecessor task non trovato"));
        Task successor = taskRepository.findByIdAndProjectId(req.successorId(), projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Successor task non trovato"));

        if (dependencyRepository.existsByPredecessorIdAndSuccessorId(req.predecessorId(), req.successorId())) {
            throw new ConflictException("Dipendenza già esistente tra questi task");
        }

        // PRD-08-FR-007: Cycle detection
        if (planningEngine.wouldCreateCycle(projectId, req.predecessorId(), req.successorId())) {
            throw new ConflictException("L'aggiunta di questa dipendenza creerebbe un ciclo");
        }

        Dependency dependency = new Dependency();
        dependency.setPredecessor(predecessor);
        dependency.setSuccessor(successor);
        dependency.setType(Dependency.DependencyType.valueOf(req.type()));
        dependency.setLag(req.lag());
        dependency.setLead(req.lead());

        Dependency saved = dependencyRepository.save(dependency);

        // PRD-08-FR-008: Recalculate plan after adding dependency
        planningEngine.recalculatePlan(projectId);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dependency> listByProject(String userId, String projectId) {
        return dependencyRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dependency> listByTask(String userId, String projectId, String taskId) {
        taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Task non trovato"));
        return dependencyRepository.findByTaskId(taskId);
    }

    @Override
    public void delete(String userId, String projectId, String dependencyId) {
        Dependency dep = dependencyRepository.findById(dependencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Dipendenza non trovata"));

        // Verify it belongs to the project
        if (!dep.getPredecessor().getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Dipendenza non trovata nel progetto");
        }

        dependencyRepository.delete(dep);

        // Recalculate after removal
        planningEngine.recalculatePlan(projectId);
    }
}
