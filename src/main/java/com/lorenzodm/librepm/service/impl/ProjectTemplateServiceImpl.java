package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateProjectTemplateRequest;
import com.lorenzodm.librepm.api.exception.ConflictException;
import com.lorenzodm.librepm.api.exception.OwnershipViolationException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.ProjectTemplate;
import com.lorenzodm.librepm.repository.ProjectTemplateRepository;
import com.lorenzodm.librepm.service.ProjectTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PRD-16: ProjectTemplate CRUD and gallery management.
 * PRD-16-BR-001: templates are not live projects.
 * PRD-16-BR-005: system templates are versionable.
 * PRD-16-FR-006: scope-based separation (SYSTEM / WORKSPACE / USER).
 */
@Service
@Transactional
public class ProjectTemplateServiceImpl implements ProjectTemplateService {

    private final ProjectTemplateRepository templateRepository;

    public ProjectTemplateServiceImpl(ProjectTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectTemplate> listAll() {
        return templateRepository.findByTemplateScopeInOrderByTemplateScopeAscNameAsc(
                List.of(ProjectTemplate.TemplateScope.SYSTEM,
                        ProjectTemplate.TemplateScope.WORKSPACE,
                        ProjectTemplate.TemplateScope.USER));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectTemplate> listSystemTemplates() {
        return templateRepository.findByTemplateScopeOrderByNameAsc(ProjectTemplate.TemplateScope.SYSTEM);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectTemplate getById(String templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template non trovato: " + templateId));
    }

    @Override
    public ProjectTemplate create(CreateProjectTemplateRequest req) {
        if (templateRepository.existsByName(req.name())) {
            throw new ConflictException("Template con questo nome già esistente");
        }

        ProjectTemplate t = new ProjectTemplate();
        t.setName(req.name());
        t.setDescription(req.description());
        t.setCategory(req.category());
        t.setUseCases(req.useCases());
        t.setPrerequisites(req.prerequisites());
        t.setRequiresPlanningEngine(req.requiresPlanningEngine());
        t.setTemplateScope(ProjectTemplate.TemplateScope.USER);
        t.setVersion("1.0");
        t.setStructureJson(req.structureJson() != null ? req.structureJson() : "{}");

        return templateRepository.save(t);
    }

    @Override
    public ProjectTemplate clone(String templateId, String newName) {
        ProjectTemplate source = getById(templateId);

        if (templateRepository.existsByName(newName)) {
            throw new ConflictException("Template con nome '" + newName + "' già esistente");
        }

        ProjectTemplate copy = new ProjectTemplate();
        copy.setName(newName);
        copy.setDescription(source.getDescription());
        copy.setCategory(source.getCategory());
        copy.setUseCases(source.getUseCases());
        copy.setPrerequisites(source.getPrerequisites());
        copy.setRequiresPlanningEngine(source.isRequiresPlanningEngine());
        copy.setTemplateScope(ProjectTemplate.TemplateScope.USER); // clone is always USER-scoped
        copy.setVersion("1.0");
        copy.setStructureJson(source.getStructureJson());

        return templateRepository.save(copy);
    }

    @Override
    public void delete(String templateId) {
        ProjectTemplate t = getById(templateId);
        // PRD-16-BR-001: system templates cannot be deleted via the API
        if (t.getTemplateScope() == ProjectTemplate.TemplateScope.SYSTEM) {
            throw new OwnershipViolationException("I template di sistema non possono essere eliminati");
        }
        templateRepository.delete(t);
    }

    @Override
    public ProjectTemplate createNewVersion(String templateId, String structureJson) {
        ProjectTemplate source = getById(templateId);

        ProjectTemplate newVersion = new ProjectTemplate();
        newVersion.setName(source.getName());
        newVersion.setDescription(source.getDescription());
        newVersion.setCategory(source.getCategory());
        newVersion.setUseCases(source.getUseCases());
        newVersion.setPrerequisites(source.getPrerequisites());
        newVersion.setRequiresPlanningEngine(source.isRequiresPlanningEngine());
        newVersion.setTemplateScope(source.getTemplateScope());
        newVersion.setCapabilityProfile(source.getCapabilityProfile());
        newVersion.setComplexityLevel(source.getComplexityLevel());

        // Parse current version and increment
        String currentVersion = source.getVersion();
        try {
            String[] parts = currentVersion.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            newVersion.setVersion(major + "." + (minor + 1));
        } catch (NumberFormatException e) {
            newVersion.setVersion(currentVersion + ".1");
        }

        newVersion.setPreviousVersionId(source.getId());
        newVersion.setStructureJson(structureJson != null ? structureJson : source.getStructureJson());

        // Mark old version name as superseded to avoid unique constraint
        source.setName(source.getName() + " (v" + source.getVersion() + ")");
        templateRepository.save(source);

        return templateRepository.save(newVersion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectTemplate> listByComplexity(String complexityLevel) {
        return templateRepository.findByComplexityLevelOrderByNameAsc(complexityLevel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectTemplate> listByCategory(String category) {
        return templateRepository.findByCategoryOrderByNameAsc(category);
    }
}
