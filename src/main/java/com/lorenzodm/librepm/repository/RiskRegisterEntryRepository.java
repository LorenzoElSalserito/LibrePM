package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.RiskRegisterEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskRegisterEntryRepository extends JpaRepository<RiskRegisterEntry, String> {

    List<RiskRegisterEntry> findByProjectId(String projectId);

    List<RiskRegisterEntry> findByProjectIdAndImpact(String projectId, RiskRegisterEntry.RiskLevel impact);
}
