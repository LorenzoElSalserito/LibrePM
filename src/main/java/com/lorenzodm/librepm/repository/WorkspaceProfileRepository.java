package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.WorkspaceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceProfileRepository extends JpaRepository<WorkspaceProfile, String> {

    List<WorkspaceProfile> findBySystemTrue();
}
