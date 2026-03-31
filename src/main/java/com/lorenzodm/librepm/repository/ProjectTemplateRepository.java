package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ProjectTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectTemplateRepository extends JpaRepository<ProjectTemplate, String> {

    /** Returns all templates visible to the gallery (system + workspace + user). */
    List<ProjectTemplate> findByTemplateScope(ProjectTemplate.TemplateScope scope);

    /** Returns all system templates (PRD-16-FR-002: built-in gallery). */
    List<ProjectTemplate> findByTemplateScopeOrderByNameAsc(ProjectTemplate.TemplateScope scope);

    Optional<ProjectTemplate> findByName(String name);

    boolean existsByName(String name);

    /** Returns all templates a user can see: SYSTEM + WORKSPACE + their USER ones. */
    List<ProjectTemplate> findByTemplateScopeInOrderByTemplateScopeAscNameAsc(
            List<ProjectTemplate.TemplateScope> scopes);

    List<ProjectTemplate> findByComplexityLevelOrderByNameAsc(String complexityLevel);

    List<ProjectTemplate> findByCategoryOrderByNameAsc(String category);
}
