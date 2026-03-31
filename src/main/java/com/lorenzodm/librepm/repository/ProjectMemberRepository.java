package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ProjectMember;
import com.lorenzodm.librepm.core.entity.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    List<ProjectMember> findByProjectId(String projectId);
    List<ProjectMember> findByUserId(String userId);
    Optional<ProjectMember> findByProjectIdAndUserId(String projectId, String userId);
    
    /**
     * Verifica efficiente se un utente è membro di un progetto.
     */
    boolean existsByProjectIdAndUserId(String projectId, String userId);
}
