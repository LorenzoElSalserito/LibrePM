package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ComplianceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceProfileRepository extends JpaRepository<ComplianceProfile, String> {
    List<ComplianceProfile> findByIsSystemTrue();
    List<ComplianceProfile> findAllByOrderByNameAsc();
}
