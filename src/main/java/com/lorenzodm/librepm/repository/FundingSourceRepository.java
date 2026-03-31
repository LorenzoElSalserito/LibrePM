package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.FundingSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundingSourceRepository extends JpaRepository<FundingSource, String> {
    List<FundingSource> findByProjectIdOrderByNameAsc(String projectId);
    List<FundingSource> findByProjectIdAndStatus(String projectId, String status);
}
