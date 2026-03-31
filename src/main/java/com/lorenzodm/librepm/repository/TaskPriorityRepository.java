package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.TaskPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskPriorityRepository extends JpaRepository<TaskPriority, String> {

    Optional<TaskPriority> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT tp FROM TaskPriority tp WHERE tp.deletedAt IS NULL ORDER BY tp.level ASC")
    List<TaskPriority> findAllActiveOrderByLevel();
}
