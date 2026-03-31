package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;

/**
 * Represents a blueprint for creating new projects (PRD-16).
 * Templates are serializable, versionable blueprints — not live projects.
 * PRD-16-BR-001: a template is NOT a live project.
 * PRD-16-BR-005: templates are versionable.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "project_templates", indexes = {
        @Index(name = "idx_template_scope", columnList = "template_scope"),
        @Index(name = "idx_template_category", columnList = "category")
})
public class ProjectTemplate extends BaseSyncEntity {

    /**
     * PRD-16-FR-006: distinguishes between system templates, workspace templates and user templates.
     */
    public enum TemplateScope {
        SYSTEM,
        WORKSPACE,
        USER
    }

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /** Category label for the gallery (e.g. PROJECT_MANAGEMENT, MARKETING, GANTT). */
    @Column(length = 50)
    private String category;

    /** Use-case description for the gallery card (PRD-16-FR-011). */
    @Column(columnDefinition = "TEXT")
    private String useCases;

    /** Prerequisites / capability requirements the workspace must support (PRD-16-BR-003/004). */
    @Column(columnDefinition = "TEXT")
    private String prerequisites;

    /** Semantic version for system template evolution (PRD-16-BR-005). */
    @Column(nullable = false, length = 20)
    private String version = "1.0";

    /** When true the template activates the planning engine (Gantt, CPM, dependencies). */
    @Column(nullable = false)
    private boolean requiresPlanningEngine;

    /** PRD-16-FR-006: SYSTEM | WORKSPACE | USER. */
    @Enumerated(EnumType.STRING)
    @Column(name = "template_scope", nullable = false, length = 20)
    private TemplateScope templateScope = TemplateScope.USER;

    /** Link to previous version for version chain (PRD-16-BR-005). */
    @Column(name = "previous_version_id", length = 36)
    private String previousVersionId;

    /** JSON describing which workspace modules are required (e.g. {"planning":true,"finance":true}). */
    @Column(name = "capability_profile", columnDefinition = "TEXT")
    private String capabilityProfile;

    /** Complexity level: SIMPLE, MODERATE, ADVANCED. */
    @Column(name = "complexity_level", length = 16)
    private String complexityLevel = "SIMPLE";

    /**
     * JSON blueprint: serialized TemplateBlueprintDto.
     * Contains phases, tasks, deliverables, OKRs, charter, view presets, tags.
     */
    @Column(columnDefinition = "TEXT")
    private String structureJson;

    public ProjectTemplate() {
        super();
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUseCases() { return useCases; }
    public void setUseCases(String useCases) { this.useCases = useCases; }
    public String getPrerequisites() { return prerequisites; }
    public void setPrerequisites(String prerequisites) { this.prerequisites = prerequisites; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public boolean isRequiresPlanningEngine() { return requiresPlanningEngine; }
    public void setRequiresPlanningEngine(boolean requiresPlanningEngine) { this.requiresPlanningEngine = requiresPlanningEngine; }
    public TemplateScope getTemplateScope() { return templateScope; }
    public void setTemplateScope(TemplateScope templateScope) { this.templateScope = templateScope; }
    public String getPreviousVersionId() { return previousVersionId; }
    public void setPreviousVersionId(String previousVersionId) { this.previousVersionId = previousVersionId; }
    public String getCapabilityProfile() { return capabilityProfile; }
    public void setCapabilityProfile(String capabilityProfile) { this.capabilityProfile = capabilityProfile; }
    public String getComplexityLevel() { return complexityLevel; }
    public void setComplexityLevel(String complexityLevel) { this.complexityLevel = complexityLevel; }
    public String getStructureJson() { return structureJson; }
    public void setStructureJson(String structureJson) { this.structureJson = structureJson; }

    /** Convenience alias kept for backward compat. */
    public boolean isSystemTemplate() { return templateScope == TemplateScope.SYSTEM; }
}
