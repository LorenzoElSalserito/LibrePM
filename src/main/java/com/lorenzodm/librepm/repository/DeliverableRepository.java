package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Deliverable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliverableRepository extends JpaRepository<Deliverable, String> {

    List<Deliverable> findByProjectId(String projectId);

    List<Deliverable> findByProjectIdAndRiskStatus(String projectId, Deliverable.RiskStatus riskStatus);
}
