package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Dependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DependencyRepository extends JpaRepository<Dependency, String> {

    List<Dependency> findByPredecessorId(String predecessorId);

    List<Dependency> findBySuccessorId(String successorId);

    boolean existsByPredecessorIdAndSuccessorId(String predecessorId, String successorId);

    @Query("SELECT d FROM Dependency d WHERE d.predecessor.project.id = :projectId")
    List<Dependency> findByProjectId(@Param("projectId") String projectId);

    @Query("SELECT d FROM Dependency d WHERE d.predecessor.id = :taskId OR d.successor.id = :taskId")
    List<Dependency> findByTaskId(@Param("taskId") String taskId);
}
