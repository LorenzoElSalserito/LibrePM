package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.EffortEstimate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EffortEstimateRepository extends JpaRepository<EffortEstimate, String> {

    @Query("SELECT ee FROM EffortEstimate ee WHERE ee.task.id = :taskId AND ee.deletedAt IS NULL ORDER BY ee.estimationDate DESC")
    List<EffortEstimate> findByTaskIdOrderByDateDesc(@Param("taskId") String taskId);

    @Query("SELECT ee FROM EffortEstimate ee WHERE ee.task.id = :taskId AND ee.deletedAt IS NULL ORDER BY ee.estimationDate DESC")
    Optional<EffortEstimate> findLatestByTaskId(@Param("taskId") String taskId);

    @Query("SELECT ee FROM EffortEstimate ee WHERE ee.estimator.id = :userId AND ee.deletedAt IS NULL ORDER BY ee.estimationDate DESC")
    List<EffortEstimate> findByEstimatorId(@Param("userId") String userId);

    @Query("SELECT ee FROM EffortEstimate ee WHERE ee.task.project.id = :projectId AND ee.deletedAt IS NULL ORDER BY ee.estimationDate DESC")
    List<EffortEstimate> findByProjectId(@Param("projectId") String projectId);
}
