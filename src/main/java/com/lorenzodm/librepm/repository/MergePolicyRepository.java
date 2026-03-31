package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.MergePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface MergePolicyRepository extends JpaRepository<MergePolicy, String> {

    Optional<MergePolicy> findByEntityType(String entityType);

    List<MergePolicy> findByPolicy(MergePolicy.PolicyType policy);
}
