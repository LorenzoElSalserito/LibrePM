package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ConflictRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConflictRecordRepository extends JpaRepository<ConflictRecord, String> {

    List<ConflictRecord> findByResolvedFalseOrderByDetectedAtDesc();

    List<ConflictRecord> findByEntityTypeAndEntityIdOrderByDetectedAtDesc(String entityType, String entityId);

    List<ConflictRecord> findByResolvedFalseAndEntityTypeOrderByDetectedAtDesc(String entityType);

    long countByResolvedFalse();
}
