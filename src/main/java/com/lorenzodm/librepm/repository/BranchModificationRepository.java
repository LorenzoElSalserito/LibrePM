package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.BranchModification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchModificationRepository extends JpaRepository<BranchModification, String> {
    List<BranchModification> findByBranchIdOrderByModifiedAtDesc(String branchId);
}
