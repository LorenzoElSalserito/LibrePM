package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.EvidencePack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidencePackRepository extends JpaRepository<EvidencePack, String> {

    List<EvidencePack> findByProjectId(String projectId);
}
