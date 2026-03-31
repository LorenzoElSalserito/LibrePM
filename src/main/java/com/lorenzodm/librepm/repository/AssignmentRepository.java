package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, String> {

    List<Assignment> findByTaskId(String taskId);

    List<Assignment> findByUserId(String userId);

    @Query("SELECT a FROM Assignment a WHERE a.task.id = :taskId AND a.user.id = :userId AND a.deletedAt IS NULL")
    Optional<Assignment> findByTaskIdAndUserId(@Param("taskId") String taskId, @Param("userId") String userId);

    boolean existsByTaskIdAndUserId(String taskId, String userId);

    @Query("SELECT a FROM Assignment a WHERE a.task.project.id = :projectId AND a.deletedAt IS NULL")
    List<Assignment> findByProjectId(@Param("projectId") String projectId);

    @Query("SELECT a FROM Assignment a WHERE a.user.id = :userId AND a.task.project.id = :projectId AND a.deletedAt IS NULL")
    List<Assignment> findByUserIdAndProjectId(@Param("userId") String userId, @Param("projectId") String projectId);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.task.id = :taskId AND a.deletedAt IS NULL")
    long countByTaskId(@Param("taskId") String taskId);

    void deleteByTaskId(String taskId);
}
