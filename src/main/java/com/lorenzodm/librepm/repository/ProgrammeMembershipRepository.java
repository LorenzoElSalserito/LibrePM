package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ProgrammeMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgrammeMembershipRepository extends JpaRepository<ProgrammeMembership, String> {
    List<ProgrammeMembership> findByProgrammeIdOrderBySortOrderAsc(String programmeId);
    List<ProgrammeMembership> findByProjectId(String projectId);
}
