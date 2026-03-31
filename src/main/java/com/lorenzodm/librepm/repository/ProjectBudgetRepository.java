package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ProjectBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectBudgetRepository extends JpaRepository<ProjectBudget, String> {
    List<ProjectBudget> findByProjectIdOrderByVersionDesc(String projectId);
    List<ProjectBudget> findByProjectIdAndStatus(String projectId, String status);
}
