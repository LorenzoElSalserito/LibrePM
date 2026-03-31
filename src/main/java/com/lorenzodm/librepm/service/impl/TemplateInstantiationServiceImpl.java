package com.lorenzodm.librepm.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorenzodm.librepm.api.dto.blueprint.*;
import com.lorenzodm.librepm.api.dto.request.InstantiateTemplateRequest;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.*;
import com.lorenzodm.librepm.repository.*;
import com.lorenzodm.librepm.service.TemplateInstantiationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Materializes a ProjectTemplate blueprint into an independent Project (PRD-16-FR-004).
 * PRD-16-BR-002: all created entities are new and independent of the template.
 * PRD-16-AC-006: modifying the project does NOT alter the template.
 */
@Service
@Transactional
public class TemplateInstantiationServiceImpl implements TemplateInstantiationService {

    private final ProjectTemplateRepository templateRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final TaskPriorityRepository taskPriorityRepository;
    private final DeliverableRepository deliverableRepository;
    private final OkrRepository okrRepository;
    private final SuccessMetricRepository successMetricRepository;
    private final ProjectCharterRepository projectCharterRepository;
    private final DependencyRepository dependencyRepository;

    private final ObjectMapper objectMapper;

    public TemplateInstantiationServiceImpl(
            ProjectTemplateRepository templateRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository,
            TaskRepository taskRepository,
            TaskStatusRepository taskStatusRepository,
            TaskPriorityRepository taskPriorityRepository,
            DeliverableRepository deliverableRepository,
            OkrRepository okrRepository,
            SuccessMetricRepository successMetricRepository,
            ProjectCharterRepository projectCharterRepository,
            DependencyRepository dependencyRepository) {
        this.templateRepository = templateRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.taskPriorityRepository = taskPriorityRepository;
        this.deliverableRepository = deliverableRepository;
        this.okrRepository = okrRepository;
        this.successMetricRepository = successMetricRepository;
        this.projectCharterRepository = projectCharterRepository;
        this.dependencyRepository = dependencyRepository;

        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Project instantiate(String userId, String templateId, InstantiateTemplateRequest req) {
        ProjectTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template non trovato: " + templateId));
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + userId));

        // 1. Create the project (PRD-16-BR-002: independent of template)
        Project project = new Project();
        project.setName(req.projectName());
        project.setDescription(req.description());
        if (req.color() != null) project.setColor(req.color());
        if (req.icon() != null)  project.setIcon(req.icon());
        project.setOwner(owner);
        project = projectRepository.save(project);

        // Add owner as OWNER member
        ProjectMember member = new ProjectMember(project, owner, ProjectMember.Role.OWNER);
        projectMemberRepository.save(member);

        // 2. Deserialize blueprint
        TemplateBlueprintDto blueprint = parseBlueprint(template.getStructureJson());
        if (blueprint == null) return project; // Nothing more to do

        // Resolve default status (TODO: configure via settings or template)
        TaskStatus defaultStatus = taskStatusRepository.findByNameIgnoreCase("TODO").orElse(null);

        // Map internal blueprint ID -> Created Task Entity
        Map<String, Task> createdTasksMap = new HashMap<>();

        // 3. Create phases (SUMMARY_TASK) + their subtasks
        int phaseOrder = 0;
        if (blueprint.phases() != null) {
            for (TemplatePhaseDto phase : blueprint.phases()) {
                Task phaseTask = new Task();
                phaseTask.setTitle(phase.name());
                phaseTask.setProject(project);
                phaseTask.setType(Task.Type.SUMMARY_TASK);
                phaseTask.setStatus(defaultStatus);
                phaseTask.setSortOrder(phaseOrder++);
                phaseTask = taskRepository.save(phaseTask);

                // Track internal ID if provided (optional for phases)
                if (phase.internalId() != null) {
                    createdTasksMap.put(phase.internalId(), phaseTask);
                }

                int taskOrder = 0;
                if (phase.tasks() != null) {
                    for (TemplateTaskDto taskDef : phase.tasks()) {
                        Task task = new Task();
                        task.setTitle(taskDef.title());
                        if (taskDef.description() != null) task.setDescription(taskDef.description());
                        task.setProject(project);
                        task.setParentTask(phaseTask);
                        task.setStatus(defaultStatus);
                        // Safe valueOf logic
                        try {
                            if (taskDef.taskType() != null) {
                                task.setType(Task.Type.valueOf(taskDef.taskType().toUpperCase()));
                            }
                        } catch (IllegalArgumentException e) {
                            task.setType(Task.Type.TASK);
                        }

                        // Resolve Priority
                        if (taskDef.priorityKey() != null) {
                            taskPriorityRepository.findByNameIgnoreCase(taskDef.priorityKey())
                                    .ifPresent(task::setPriority);
                        }

                        if (taskDef.estimatedEffort() != null) task.setEstimatedEffort(taskDef.estimatedEffort());
                        task.setSortOrder(taskOrder++);
                        task = taskRepository.save(task);

                        // Track internal ID for dependency mapping
                        if (taskDef.internalId() != null) {
                            createdTasksMap.put(taskDef.internalId(), task);
                        }
                    }
                }
            }
        }

        // 3b. Create Dependencies (PRD-08, PRD-16)
        if (blueprint.dependencies() != null) {
            for (TemplateDependencyDto depDef : blueprint.dependencies()) {
                Task fromTask = createdTasksMap.get(depDef.fromTaskInternalId());
                Task toTask = createdTasksMap.get(depDef.toTaskInternalId());

                if (fromTask != null && toTask != null) {
                    Dependency dependency = new Dependency();
                    // dependency.setProject(project); // Removed: Dependency does not have setProject
                    dependency.setPredecessor(fromTask);
                    dependency.setSuccessor(toTask);
                    if (depDef.type() != null) dependency.setType(depDef.type());
                    if (depDef.lagMinutes() != null) dependency.setLag(depDef.lagMinutes()); // Corrected setLagMinutes -> setLag
                    dependencyRepository.save(dependency);
                }
            }
        }

        // 4. Create deliverables
        if (blueprint.deliverables() != null) {
            for (TemplateDeliverableDto delivDef : blueprint.deliverables()) {
                Deliverable deliverable = new Deliverable();
                deliverable.setProject(project);
                deliverable.setName(delivDef.name());
                deliverable.setDescription(delivDef.description());
                deliverable.setProgress(0);
                deliverableRepository.save(deliverable);
            }
        }

        // 5. Create OKRs with key results
        if (blueprint.okrs() != null) {
            for (TemplateOkrDto okrDef : blueprint.okrs()) {
                Okr okr = new Okr();
                okr.setProject(project);
                okr.setObjective(okrDef.objective());
                okr = okrRepository.save(okr);

                if (okrDef.keyResults() != null) {
                    for (TemplateMetricDto metricDef : okrDef.keyResults()) {
                        SuccessMetric metric = new SuccessMetric();
                        metric.setOkr(okr);
                        metric.setName(metricDef.name());
                        metric.setTargetValue(metricDef.targetValue());
                        metric.setCurrentValue(0.0);
                        metric.setUnit(metricDef.unit());
                        successMetricRepository.save(metric);
                    }
                }
            }
        }

        // 6. Create project charter if defined
        if (blueprint.charter() != null) {
            TemplateCharterDto charterDef = blueprint.charter();
            ProjectCharter charter = new ProjectCharter();
            charter.setProject(project);
            if (charterDef.objectives() != null)       charter.setObjectives(charterDef.objectives());
            if (charterDef.problemStatement() != null) charter.setProblemStatement(charterDef.problemStatement());
            if (charterDef.businessCase() != null)     charter.setBusinessCase(charterDef.businessCase());
            projectCharterRepository.save(charter);
        }

        return project;
    }

    // --- Private helpers ---

    private TemplateBlueprintDto parseBlueprint(String json) {
        if (json == null || json.isBlank() || json.equals("{}")) {
            return null;
        }
        try {
            return objectMapper.readValue(json, TemplateBlueprintDto.class);
        } catch (Exception e) {
            // Log error in real app
            return null;
        }
    }
}
