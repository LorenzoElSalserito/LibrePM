package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.InstantiateTemplateRequest;
import com.lorenzodm.librepm.core.entity.Project;

/**
 * Materializes a ProjectTemplate blueprint into a real, live project (PRD-16-FR-004).
 * PRD-16-BR-002: the created project is fully independent of the source template.
 * PRD-16-AC-006: modifications to the project do NOT alter the template.
 */
public interface TemplateInstantiationService {

    /**
     * Creates a new Project (with tasks, deliverables, OKRs, charter) from the
     * specified template blueprint. The result is a standalone project owned by userId.
     */
    Project instantiate(String userId, String templateId, InstantiateTemplateRequest req);
}
