package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.SponsorCommitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SponsorCommitmentRepository extends JpaRepository<SponsorCommitment, String> {
    List<SponsorCommitment> findByProjectIdOrderBySponsorIdAsc(String projectId);
    List<SponsorCommitment> findBySponsorId(String sponsorId);
}
