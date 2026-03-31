package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ProgrammeMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgrammeMilestoneRepository extends JpaRepository<ProgrammeMilestone, String> {
    List<ProgrammeMilestone> findByProgrammeIdOrderByTargetDateAsc(String programmeId);
}
