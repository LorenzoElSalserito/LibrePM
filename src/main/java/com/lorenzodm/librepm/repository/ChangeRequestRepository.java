package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, String> {
    List<ChangeRequest> findByProjectIdOrderByRequestedAtDesc(String projectId);
    List<ChangeRequest> findByProjectIdAndStatus(String projectId, String status);
}
