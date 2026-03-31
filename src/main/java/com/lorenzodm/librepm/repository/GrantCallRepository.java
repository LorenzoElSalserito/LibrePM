package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.GrantCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrantCallRepository extends JpaRepository<GrantCall, String> {
    List<GrantCall> findByProjectIdOrderByDeadlineAsc(String projectId);
    List<GrantCall> findByProjectIdAndStatus(String projectId, String status);
}
