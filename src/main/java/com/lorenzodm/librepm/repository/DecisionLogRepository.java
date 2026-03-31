package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.DecisionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DecisionLogRepository extends JpaRepository<DecisionLog, String> {
    List<DecisionLog> findByProjectIdOrderByDecidedAtDesc(String projectId);
}
