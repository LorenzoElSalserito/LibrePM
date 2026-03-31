package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, String> {

    @Query("SELECT ar FROM ApprovalRequest ar WHERE ar.approver.id = :userId AND ar.status = 'PENDING' AND ar.deletedAt IS NULL ORDER BY ar.requestedAt DESC")
    List<ApprovalRequest> findPendingByApproverId(@Param("userId") String userId);

    @Query("SELECT ar FROM ApprovalRequest ar WHERE ar.requestedBy.id = :userId AND ar.deletedAt IS NULL ORDER BY ar.requestedAt DESC")
    List<ApprovalRequest> findByRequestedById(@Param("userId") String userId);

    @Query("SELECT ar FROM ApprovalRequest ar WHERE ar.project.id = :projectId AND ar.deletedAt IS NULL ORDER BY ar.requestedAt DESC")
    List<ApprovalRequest> findByProjectId(@Param("projectId") String projectId);

    @Query("SELECT COUNT(ar) FROM ApprovalRequest ar WHERE ar.approver.id = :userId AND ar.status = 'PENDING' AND ar.deletedAt IS NULL")
    long countPendingByApproverId(@Param("userId") String userId);
}
