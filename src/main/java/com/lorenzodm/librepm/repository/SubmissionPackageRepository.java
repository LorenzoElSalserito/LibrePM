package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.SubmissionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionPackageRepository extends JpaRepository<SubmissionPackage, String> {
    List<SubmissionPackage> findByCallIdOrderByCreatedAtDesc(String callId);
}
