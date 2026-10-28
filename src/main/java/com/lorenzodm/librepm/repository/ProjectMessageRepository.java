package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ProjectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectMessageRepository extends JpaRepository<ProjectMessage, String> {
    Page<ProjectMessage> findByProjectIdOrderByCreatedAtDescIdDesc(String projectId, Pageable pageable);
}
