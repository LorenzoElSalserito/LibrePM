package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ResourceAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, String> {

    List<ResourceAllocation> findByUserId(String userId);

    List<ResourceAllocation> findByProjectId(String projectId);

    @Query("SELECT ra FROM ResourceAllocation ra WHERE ra.user.id = :userId " +
           "AND ra.startDate <= :periodEnd AND ra.endDate >= :periodStart")
    List<ResourceAllocation> findByUserIdAndPeriodOverlap(
            @Param("userId") String userId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );

    @Query("SELECT ra FROM ResourceAllocation ra WHERE ra.project.id = :projectId " +
           "AND ra.startDate <= :periodEnd AND ra.endDate >= :periodStart")
    List<ResourceAllocation> findByProjectIdAndPeriodOverlap(
            @Param("projectId") String projectId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );
}
