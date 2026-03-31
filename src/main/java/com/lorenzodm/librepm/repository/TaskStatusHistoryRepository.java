package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.TaskStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for task status history (PRD-01-FR-008).
 *
 * @author Lorenzo DM
 * @since 0.10.0
 */
@Repository
public interface TaskStatusHistoryRepository extends JpaRepository<TaskStatusHistory, String> {

    List<TaskStatusHistory> findByTaskIdOrderByChangedAtDesc(String taskId);

    List<TaskStatusHistory> findByChangedByOrderByChangedAtDesc(String userId);
}
