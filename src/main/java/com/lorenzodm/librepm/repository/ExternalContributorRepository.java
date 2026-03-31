package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ExternalContributor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalContributorRepository extends JpaRepository<ExternalContributor, String> {

    @Query("SELECT ec FROM ExternalContributor ec WHERE ec.scopeEntityId = :entityId AND ec.deletedAt IS NULL ORDER BY ec.displayName")
    List<ExternalContributor> findByScopeEntityId(@Param("entityId") String entityId);

    @Query("SELECT ec FROM ExternalContributor ec WHERE ec.createdBy.id = :userId AND ec.deletedAt IS NULL ORDER BY ec.displayName")
    List<ExternalContributor> findByCreatedById(@Param("userId") String userId);

    @Query("SELECT ec FROM ExternalContributor ec WHERE ec.deletedAt IS NULL ORDER BY ec.displayName")
    List<ExternalContributor> findAllActive();
}
