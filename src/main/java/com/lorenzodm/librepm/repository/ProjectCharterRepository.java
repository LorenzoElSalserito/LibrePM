package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ProjectCharter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectCharterRepository extends JpaRepository<ProjectCharter, String> {

    Optional<ProjectCharter> findByProjectId(String projectId);

    boolean existsByProjectId(String projectId);
}
