package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.RetentionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetentionPolicyRepository extends JpaRepository<RetentionPolicy, String> {
    List<RetentionPolicy> findByDataCategoryId(String dataCategoryId);
    List<RetentionPolicy> findByIsActiveTrue();
}
