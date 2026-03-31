package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ObligationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObligationRecordRepository extends JpaRepository<ObligationRecord, String> {
    List<ObligationRecord> findByProjectIdOrderByDeadlineAsc(String projectId);
    List<ObligationRecord> findByProjectIdAndStatus(String projectId, String status);
}
