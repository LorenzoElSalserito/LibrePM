package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.StatusReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusReviewRepository extends JpaRepository<StatusReview, String> {

    List<StatusReview> findByProjectIdOrderByReviewDateDesc(String projectId);

    @Query("SELECT sr FROM StatusReview sr WHERE sr.project.id = :projectId ORDER BY sr.reviewDate DESC LIMIT 1")
    Optional<StatusReview> findLatestByProjectId(String projectId);
}
