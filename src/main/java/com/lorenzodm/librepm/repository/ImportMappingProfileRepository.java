package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ImportMappingProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportMappingProfileRepository extends JpaRepository<ImportMappingProfile, String> {

    List<ImportMappingProfile> findByEntityType(String entityType);

    List<ImportMappingProfile> findByCreatedBy(String userId);
}
