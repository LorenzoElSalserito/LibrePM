package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.CallRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallRequirementRepository extends JpaRepository<CallRequirement, String> {
    List<CallRequirement> findByCallIdOrderBySortOrderAsc(String callId);
}
