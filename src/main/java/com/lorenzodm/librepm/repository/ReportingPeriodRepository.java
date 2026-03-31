package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ReportingPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportingPeriodRepository extends JpaRepository<ReportingPeriod, String> {
    List<ReportingPeriod> findByProjectIdOrderByDueDateAsc(String projectId);
}
