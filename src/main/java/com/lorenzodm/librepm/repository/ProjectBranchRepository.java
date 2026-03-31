package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ProjectBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectBranchRepository extends JpaRepository<ProjectBranch, String> {
    List<ProjectBranch> findBySourceProjectIdOrderByCreatedAtDesc(String sourceProjectId);
}
