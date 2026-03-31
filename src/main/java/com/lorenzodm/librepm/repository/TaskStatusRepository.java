package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, String> {

    Optional<TaskStatus> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT ts FROM TaskStatus ts WHERE ts.deletedAt IS NULL ORDER BY ts.name ASC")
    List<TaskStatus> findAllActive();

    @Query("SELECT ts FROM TaskStatus ts WHERE ts.deletedAt IS NULL AND LOWER(ts.name) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<TaskStatus> searchByName(@Param("term") String term);
}
