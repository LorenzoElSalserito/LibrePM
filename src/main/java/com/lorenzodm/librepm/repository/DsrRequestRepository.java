package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.DsrRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DsrRequestRepository extends JpaRepository<DsrRequest, String> {
    List<DsrRequest> findByUserIdOrderByRequestedAtDesc(String userId);
    List<DsrRequest> findByStatus(String status);
}
