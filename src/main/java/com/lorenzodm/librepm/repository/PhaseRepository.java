package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Phase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhaseRepository extends JpaRepository<Phase, String> {

    List<Phase> findByProjectIdOrderBySortOrderAsc(String projectId);
}
